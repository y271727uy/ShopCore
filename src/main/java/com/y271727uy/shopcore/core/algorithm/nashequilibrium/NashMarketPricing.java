package com.y271727uy.shopcore.core.algorithm.nashequilibrium;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime bridge between the order system and {@link NashEquilibrium}.
 * <p>
 * Markets are demand categories; strategies are item ids inside a category.
 * Order candidates register strategies, completed order lines record volume,
 * and once per window every market is rebalanced. The resulting multiplier is
 * applied to automatically priced order lines.
 * <p>
 * State lives in memory only and resets on server restart.
 */
public final class NashMarketPricing {
    /** One Minecraft day. */
    public static final long DEFAULT_WINDOW_TICKS = 24000L;

    private static final NashMarketPricing INSTANCE = new NashMarketPricing(EquilibriumConfig.DEFAULT, DEFAULT_WINDOW_TICKS);

    private final EquilibriumConfig config;
    private final long windowTicks;
    private final Map<String, Map<String, StrategyState>> markets = new LinkedHashMap<>();
    private final Map<String, EquilibriumSnapshot> lastSnapshots = new LinkedHashMap<>();
    private long windowStartTick = Long.MIN_VALUE;

    public NashMarketPricing(EquilibriumConfig config, long windowTicks) {
        this.config = Objects.requireNonNull(config, "config");
        if (windowTicks <= 0L) {
            throw new IllegalArgumentException("windowTicks must be positive");
        }
        this.windowTicks = windowTicks;
    }

    public static NashMarketPricing instance() {
        return INSTANCE;
    }

    /**
     * Registers an item as a competing strategy in a market so that unsold items
     * still take part in balancing.
     */
    public synchronized void registerStrategy(ResourceLocation market, ResourceLocation item) {
        String marketKey = market.toString();
        String strategyKey = item.toString();
        markets.computeIfAbsent(marketKey, key -> new LinkedHashMap<>())
                .computeIfAbsent(strategyKey, key -> new StrategyState());
    }

    /**
     * Records a completed sale. Items that were never registered are ignored.
     */
    public synchronized void recordSale(ResourceLocation item, int quantity, int unitPrice, long nowTick) {
        rebalanceIfDue(nowTick);
        if (quantity <= 0) {
            return;
        }
        String strategyKey = item.toString();
        long saleVolume = quantity;
        double saleIncome = Math.max(0, unitPrice) * (double) quantity;
        for (Map<String, StrategyState> strategies : markets.values()) {
            StrategyState state = strategies.get(strategyKey);
            if (state == null) {
                continue;
            }
            state.volume += saleVolume;
            state.income += saleIncome;
        }
    }

    /**
     * Returns the current multiplier for an item in a market; 1.0 when unknown.
     */
    public synchronized double multiplier(ResourceLocation market, ResourceLocation item, long nowTick) {
        rebalanceIfDue(nowTick);
        Map<String, StrategyState> strategies = markets.get(market.toString());
        if (strategies == null) {
            return 1.0D;
        }
        StrategyState state = strategies.get(item.toString());
        return state == null ? 1.0D : Math.max(0.0D, 1.0D + state.modifier);
    }

    public int applyTo(ResourceLocation market, ResourceLocation item, int basePrice, long nowTick) {
        if (basePrice <= 0) {
            return basePrice;
        }
        double adjusted = basePrice * multiplier(market, item, nowTick);
        return Math.max(1, (int) Math.round(adjusted));
    }

    public synchronized Map<String, EquilibriumSnapshot> lastSnapshots() {
        return Map.copyOf(lastSnapshots);
    }

    public synchronized void clear() {
        markets.clear();
        lastSnapshots.clear();
        windowStartTick = Long.MIN_VALUE;
    }

    private void rebalanceIfDue(long nowTick) {
        if (windowStartTick == Long.MIN_VALUE || nowTick < windowStartTick) {
            windowStartTick = nowTick;
            return;
        }
        if (nowTick - windowStartTick < windowTicks) {
            return;
        }
        windowStartTick = nowTick;
        rebalanceAll();
    }

    private void rebalanceAll() {
        for (Map.Entry<String, Map<String, StrategyState>> market : markets.entrySet()) {
            Map<String, StrategyState> strategies = market.getValue();
            // Balancing needs at least two competing strategies.
            if (strategies.size() < 2) {
                strategies.values().forEach(StrategyState::resetWindow);
                continue;
            }

            List<MarketSignal> signals = new ArrayList<>(strategies.size());
            for (Map.Entry<String, StrategyState> strategy : strategies.entrySet()) {
                StrategyState state = strategy.getValue();
                double incomePerUnit = state.volume == 0L ? 0.0D : state.income / state.volume;
                signals.add(NashEquilibrium.signal(market.getKey(), strategy.getKey(), state.volume, incomePerUnit, state.modifier));
            }

            EquilibriumSnapshot snapshot = NashEquilibrium.balance(signals, config);
            for (PriceAdjustment adjustment : snapshot.adjustments()) {
                StrategyState state = strategies.get(adjustment.key().strategyKey());
                state.modifier = adjustment.newModifier();
                state.resetWindow();
            }
            lastSnapshots.put(market.getKey(), snapshot);
        }
    }

    private static final class StrategyState {
        private long volume;
        private double income;
        private double modifier;

        private void resetWindow() {
            volume = 0L;
            income = 0.0D;
        }
    }
}

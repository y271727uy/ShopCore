package com.y271727uy.shopcore.economic.algorithm.nashequilibrium;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Pure controller that converts market observations into price modifiers.
 * <p>
 * The model is intentionally simple: overused strategies lose modifier,
 * underused strategies gain modifier, and all movement is smoothed and bounded.
 */
public final class EquilibriumController {
    private EquilibriumController() {
    }

    public static EquilibriumSnapshot update(List<MarketSignal> signals) {
        return update(signals, EquilibriumConfig.DEFAULT);
    }

    public static EquilibriumSnapshot update(List<MarketSignal> signals, EquilibriumConfig config) {
        Objects.requireNonNull(signals, "signals");
        Objects.requireNonNull(config, "config");
        if (signals.isEmpty()) {
            throw new IllegalArgumentException("signals cannot be empty");
        }

        List<MarketSignal> orderedSignals = signals.stream()
                .map(signal -> Objects.requireNonNull(signal, "signal"))
                .sorted(Comparator.comparing(signal -> signal.key().strategyKey()))
                .toList();

        String marketKey = orderedSignals.get(0).key().marketKey();
        for (MarketSignal signal : orderedSignals) {
            if (!marketKey.equals(signal.key().marketKey())) {
                throw new IllegalArgumentException("all signals must belong to the same market");
            }
        }

        double targetShare = 1.0D / orderedSignals.size();
        long totalVolume = orderedSignals.stream().mapToLong(MarketSignal::volume).sum();
        List<PriceAdjustment> adjustments = new ArrayList<>(orderedSignals.size());

        for (MarketSignal signal : orderedSignals) {
            double actualShare = totalVolume == 0L ? 0.0D : (double) signal.volume() / (double) totalVolume;
            double imbalance = targetShare - actualShare;
            double desiredModifier = imbalance * config.sensitivity();
            if (!signal.hasActivity()) {
                desiredModifier += config.inactiveBonus();
            }

            double blendedModifier = signal.currentModifier() * config.inertia()
                    + desiredModifier * (1.0D - config.inertia());
            double steppedModifier = moveWithinStep(signal.currentModifier(), blendedModifier, config.maxStep());
            double newModifier = clamp(steppedModifier, config.minModifier(), config.maxModifier());
            double priceMultiplier = Math.max(0.0D, 1.0D + newModifier);

            adjustments.add(new PriceAdjustment(
                    signal.key(),
                    targetShare,
                    actualShare,
                    signal.currentModifier(),
                    newModifier,
                    priceMultiplier,
                    imbalance
            ));
        }

        return new EquilibriumSnapshot(marketKey, adjustments, score(orderedSignals, adjustments, targetShare));
    }

    private static EquilibriumScore score(
            List<MarketSignal> signals,
            List<PriceAdjustment> adjustments,
            double targetShare
    ) {
        double concentration = 0.0D;
        for (PriceAdjustment adjustment : adjustments) {
            concentration += Math.abs(adjustment.actualShare() - targetShare);
        }
        concentration /= adjustments.size();

        double incomeSpread = incomeSpread(signals);
        double volatility = 0.0D;
        for (PriceAdjustment adjustment : adjustments) {
            volatility += Math.abs(adjustment.newModifier() - adjustment.oldModifier());
        }
        volatility /= adjustments.size();

        double value = (concentration * 0.60D) + (incomeSpread * 0.30D) + (volatility * 0.10D);
        return new EquilibriumScore(value, concentration, incomeSpread, volatility);
    }

    private static double incomeSpread(List<MarketSignal> signals) {
        double max = 0.0D;
        double min = Double.MAX_VALUE;
        boolean hasIncome = false;

        for (MarketSignal signal : signals) {
            if (signal.incomePerUnit() <= 0.0D) {
                continue;
            }
            hasIncome = true;
            max = Math.max(max, signal.incomePerUnit());
            min = Math.min(min, signal.incomePerUnit());
        }

        if (!hasIncome || max <= 0.0D) {
            return 0.0D;
        }
        return (max - min) / max;
    }

    private static double moveWithinStep(double current, double target, double maxStep) {
        if (maxStep <= 0.0D) {
            return current;
        }
        double delta = clamp(target - current, -maxStep, maxStep);
        return current + delta;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

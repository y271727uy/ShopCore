package com.y271727uy.shopcore.economic.algorithm.nashequilibrium;

import java.util.List;

/**
 * Small public entry point for the simplified Nash-like market balancer.
 */
public final class NashEquilibrium {
    private NashEquilibrium() {
    }

    /**
     * Balances one market group with the default tuning.
     */
    public static EquilibriumSnapshot balance(List<MarketSignal> signals) {
        return EquilibriumController.update(signals);
    }

    /**
     * Balances one market group with caller-provided tuning.
     */
    public static EquilibriumSnapshot balance(List<MarketSignal> signals, EquilibriumConfig config) {
        return EquilibriumController.update(signals, config);
    }

    /**
     * Convenience factory for one observed strategy signal.
     */
    public static MarketSignal signal(
            String marketKey,
            String strategyKey,
            long volume,
            double incomePerUnit,
            double currentModifier
    ) {
        return MarketSignal.of(
                StrategyKey.of(marketKey, strategyKey),
                volume,
                incomePerUnit,
                currentModifier
        );
    }
}

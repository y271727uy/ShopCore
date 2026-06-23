package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.nashequilibrium;

import java.util.Objects;

/**
 * Observed behavior for one strategy during the latest market window.
 *
 * @param key             strategy identity
 * @param volume          produced, sold, traded, or otherwise chosen amount
 * @param incomePerUnit   observed unit income after module-specific calculation
 * @param currentModifier current price modifier, where 0.10 means +10% and -0.10 means -10%
 */
public record MarketSignal(
        StrategyKey key,
        long volume,
        double incomePerUnit,
        double currentModifier
) {
    public MarketSignal {
        Objects.requireNonNull(key, "key");
        if (volume < 0L) {
            throw new IllegalArgumentException("volume cannot be negative");
        }
        if (!Double.isFinite(incomePerUnit) || incomePerUnit < 0.0D) {
            throw new IllegalArgumentException("incomePerUnit must be a finite non-negative value");
        }
        if (!Double.isFinite(currentModifier)) {
            throw new IllegalArgumentException("currentModifier must be finite");
        }
    }

    public static MarketSignal of(StrategyKey key, long volume, double incomePerUnit, double currentModifier) {
        return new MarketSignal(key, volume, incomePerUnit, currentModifier);
    }

    public boolean hasActivity() {
        return volume > 0L;
    }
}

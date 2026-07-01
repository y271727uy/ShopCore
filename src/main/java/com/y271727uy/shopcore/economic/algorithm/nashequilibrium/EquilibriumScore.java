package com.y271727uy.shopcore.economic.algorithm.nashequilibrium;

/**
 * A lightweight stability score for one market update.
 *
 * @param value        lower is more stable
 * @param concentration volume concentration component
 * @param incomeSpread unit income spread component
 * @param volatility   price movement component
 */
public record EquilibriumScore(
        double value,
        double concentration,
        double incomeSpread,
        double volatility
) {
    public EquilibriumScore {
        requireFiniteNonNegative(value, "value");
        requireFiniteNonNegative(concentration, "concentration");
        requireFiniteNonNegative(incomeSpread, "incomeSpread");
        requireFiniteNonNegative(volatility, "volatility");
    }

    public boolean isStable(EquilibriumConfig config) {
        return value <= config.equilibriumTolerance();
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
        }
    }
}

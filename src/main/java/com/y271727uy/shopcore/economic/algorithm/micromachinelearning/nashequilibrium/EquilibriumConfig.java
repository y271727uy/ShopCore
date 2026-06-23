package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.nashequilibrium;

/**
 * Tunable constants for the simplified Nash-like price balancer.
 *
 * @param sensitivity          converts share imbalance into target modifier pressure
 * @param inertia              keeps part of the old modifier to avoid violent price swings
 * @param maxStep              maximum absolute modifier movement in one update
 * @param minModifier          lower modifier bound, for example -0.50 means at most -50%
 * @param maxModifier          upper modifier bound, for example 0.50 means at most +50%
 * @param inactiveBonus        small positive pressure for strategies with zero observed activity
 * @param equilibriumTolerance score threshold used by callers to consider a market stable
 */
public record EquilibriumConfig(
        double sensitivity,
        double inertia,
        double maxStep,
        double minModifier,
        double maxModifier,
        double inactiveBonus,
        double equilibriumTolerance
) {
    public static final EquilibriumConfig DEFAULT = new EquilibriumConfig(
            0.65D,
            0.80D,
            0.08D,
            -0.50D,
            0.50D,
            0.04D,
            0.05D
    );

    public EquilibriumConfig {
        requireFiniteNonNegative(sensitivity, "sensitivity");
        requireRange(inertia, "inertia", 0.0D, 1.0D);
        requireFiniteNonNegative(maxStep, "maxStep");
        requireFinite(inactiveBonus, "inactiveBonus");
        requireFiniteNonNegative(equilibriumTolerance, "equilibriumTolerance");
        requireFinite(minModifier, "minModifier");
        requireFinite(maxModifier, "maxModifier");
        if (minModifier > maxModifier) {
            throw new IllegalArgumentException("minModifier cannot be greater than maxModifier");
        }
    }

    private static void requireFiniteNonNegative(double value, String name) {
        requireFinite(value, name);
        if (value < 0.0D) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }

    private static void requireRange(double value, String name, double min, double max) {
        requireFinite(value, name);
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be in [" + min + ", " + max + "]");
        }
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}

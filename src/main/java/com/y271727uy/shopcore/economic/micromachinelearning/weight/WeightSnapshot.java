package com.y271727uy.shopcore.economic.micromachinelearning.weight;

/**
 * Immutable view of one item weight inside one player space.
 *
 * @param weight            current decayed weight
 * @param pressure          normalized pressure in [0, 1)
 * @param penaltyMultiplier multiplier for sell price style penalties
 */
public record WeightSnapshot(
        double weight,
        double pressure,
        double penaltyMultiplier
) {
    public static final WeightSnapshot ZERO = new WeightSnapshot(0.0D, 0.0D, 1.0D);

    public WeightSnapshot {
        requireFiniteNonNegative(weight, "weight");
        requireRange(pressure, "pressure", 0.0D, 1.0D);
        requireRange(penaltyMultiplier, "penaltyMultiplier", 0.0D, 1.0D);
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
        }
    }

    private static void requireRange(double value, String name, double min, double max) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(name + " must be in [" + min + ", " + max + "]");
        }
    }
}

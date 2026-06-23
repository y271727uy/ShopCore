package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model;

/**
 * Tunable constants for a tiny online linear regressor.
 *
 * @param learningRate      step size for each update
 * @param l2Penalty         small weight decay to keep coefficients bounded
 * @param maxAbsoluteWeight coefficient clamp
 */
public record LinearModelConfig(
        double learningRate,
        double l2Penalty,
        double maxAbsoluteWeight
) {
    public static final LinearModelConfig DEFAULT = new LinearModelConfig(
            0.01D,
            0.0001D,
            10.0D
    );

    public LinearModelConfig {
        requirePositive(learningRate, "learningRate");
        requireFiniteNonNegative(l2Penalty, "l2Penalty");
        requirePositive(maxAbsoluteWeight, "maxAbsoluteWeight");
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite positive value");
        }
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
        }
    }
}

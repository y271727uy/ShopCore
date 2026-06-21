package com.y271727uy.shopcore.economic.micromachinelearning.weight;

import com.y271727uy.shopcore.economic.micromachinelearning.model.FeatureVector;

import java.util.Objects;

/**
 * Feature builder for experiments that estimate a sell-price penalty score.
 */
public final class WeightPenaltyFeatures {
    public static final int FEATURE_COUNT = 5;

    private WeightPenaltyFeatures() {
    }

    public static FeatureVector of(WeightSnapshot snapshot, double soldAmount, double candidateMultiplier) {
        Objects.requireNonNull(snapshot, "snapshot");
        requireFiniteNonNegative(soldAmount, "soldAmount");
        requireRange(candidateMultiplier, "candidateMultiplier", 0.0D, 1.0D);

        return FeatureVector.of(
                snapshot.pressure(),
                snapshot.penaltyMultiplier(),
                candidateMultiplier,
                Math.log1p(snapshot.weight()),
                Math.log1p(soldAmount)
        );
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

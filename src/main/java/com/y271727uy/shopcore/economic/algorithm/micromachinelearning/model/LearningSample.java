package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model;

import java.util.Objects;

/**
 * One supervised sample for a tiny online model.
 *
 * @param features input features
 * @param target   expected value
 * @param weight   sample importance
 */
public record LearningSample(
        FeatureVector features,
        double target,
        double weight
) {
    public LearningSample {
        Objects.requireNonNull(features, "features");
        if (!Double.isFinite(target)) {
            throw new IllegalArgumentException("target must be finite");
        }
        if (!Double.isFinite(weight) || weight < 0.0D) {
            throw new IllegalArgumentException("weight must be a finite non-negative value");
        }
    }

    public static LearningSample of(FeatureVector features, double target) {
        return new LearningSample(features, target, 1.0D);
    }
}

package com.y271727uy.shopcore.economic.micromachinelearning.model;

import java.util.Objects;

/**
 * One candidate option with the features used to estimate its future score.
 *
 * @param value    candidate value, such as a multiplier or config choice
 * @param features numeric context for the model
 * @param <T>      candidate value type
 */
public record ScoreCandidate<T>(
        T value,
        FeatureVector features
) {
    public ScoreCandidate {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(features, "features");
    }
}

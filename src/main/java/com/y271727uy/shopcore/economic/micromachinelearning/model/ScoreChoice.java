package com.y271727uy.shopcore.economic.micromachinelearning.model;

import java.util.Objects;

/**
 * Selected candidate and the model-estimated score. Lower score is better.
 *
 * @param value          selected candidate value
 * @param predictedScore estimated score for that candidate
 * @param <T>            candidate value type
 */
public record ScoreChoice<T>(
        T value,
        double predictedScore
) {
    public ScoreChoice {
        Objects.requireNonNull(value, "value");
        if (!Double.isFinite(predictedScore)) {
            throw new IllegalArgumentException("predictedScore must be finite");
        }
    }
}

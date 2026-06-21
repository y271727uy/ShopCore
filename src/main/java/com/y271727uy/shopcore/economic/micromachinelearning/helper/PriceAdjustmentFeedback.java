package com.y271727uy.shopcore.economic.micromachinelearning.helper;

import com.y271727uy.shopcore.economic.micromachinelearning.weight.WeightSnapshot;

import java.util.Objects;

/**
 * Actual outcome used to teach the tiny score model after a module observes the result.
 */
public record PriceAdjustmentFeedback(
        WeightSnapshot weightSnapshot,
        double weightAmount,
        double usedMultiplier,
        double actualScore
) {
    public PriceAdjustmentFeedback {
        Objects.requireNonNull(weightSnapshot, "weightSnapshot");
        if (!Double.isFinite(weightAmount) || weightAmount < 0.0D) {
            throw new IllegalArgumentException("weightAmount must be a finite non-negative value");
        }
        if (!Double.isFinite(usedMultiplier) || usedMultiplier < 0.0D || usedMultiplier > 1.0D) {
            throw new IllegalArgumentException("usedMultiplier must be in [0.0, 1.0]");
        }
        if (!Double.isFinite(actualScore)) {
            throw new IllegalArgumentException("actualScore must be finite");
        }
    }
}

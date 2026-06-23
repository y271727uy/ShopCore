package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper;

import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.weight.WeightSnapshot;

import java.util.Objects;

/**
 * Result returned to an external module after sell-price pressure is applied.
 */
public record PriceAdjustmentResult(
        int baseSellPrice,
        int adjustedSellPrice,
        double multiplier,
        WeightSnapshot beforeWeight,
        WeightSnapshot afterWeight,
        boolean modelSelected
) {
    public PriceAdjustmentResult {
        if (baseSellPrice < 0) {
            throw new IllegalArgumentException("baseSellPrice cannot be negative");
        }
        if (adjustedSellPrice < 0) {
            throw new IllegalArgumentException("adjustedSellPrice cannot be negative");
        }
        if (!Double.isFinite(multiplier) || multiplier < 0.0D || multiplier > 1.0D) {
            throw new IllegalArgumentException("multiplier must be in [0.0, 1.0]");
        }
        Objects.requireNonNull(beforeWeight, "beforeWeight");
        Objects.requireNonNull(afterWeight, "afterWeight");
    }
}

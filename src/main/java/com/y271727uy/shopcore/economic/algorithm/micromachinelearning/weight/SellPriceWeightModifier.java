package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.weight;

import java.util.Objects;

/**
 * Applies usage pressure in the sell-price direction: higher weight means lower sell value.
 */
public final class SellPriceWeightModifier {
    private SellPriceWeightModifier() {
    }

    public static double multiplier(WeightSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return snapshot.penaltyMultiplier();
    }

    public static int applyTo(int baseSellPrice, WeightSnapshot snapshot) {
        if (baseSellPrice <= 0) {
            return 0;
        }
        return Math.max(0, (int) Math.floor(baseSellPrice * multiplier(snapshot)));
    }
}

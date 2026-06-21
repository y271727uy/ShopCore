package com.y271727uy.shopcore.economic.micromachinelearning.helper;

import java.util.Objects;

/**
 * Request from an external module that wants a sell-price adjustment.
 *
 * @param playerKey     player identity
 * @param itemKey       item or strategy identity
 * @param baseSellPrice original sell price before pressure adjustment
 * @param weightAmount  amount to add into the player's item weight table
 * @param nowTick       current logical tick
 * @param <P>           player key type
 * @param <K>           item or strategy key type
 */
public record PriceAdjustmentRequest<P, K>(
        P playerKey,
        K itemKey,
        int baseSellPrice,
        double weightAmount,
        long nowTick
) {
    public PriceAdjustmentRequest {
        Objects.requireNonNull(playerKey, "playerKey");
        Objects.requireNonNull(itemKey, "itemKey");
        if (baseSellPrice < 0) {
            throw new IllegalArgumentException("baseSellPrice cannot be negative");
        }
        if (!Double.isFinite(weightAmount) || weightAmount < 0.0D) {
            throw new IllegalArgumentException("weightAmount must be a finite non-negative value");
        }
    }
}

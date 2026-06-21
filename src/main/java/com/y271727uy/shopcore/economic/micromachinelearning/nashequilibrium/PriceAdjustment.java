package com.y271727uy.shopcore.economic.micromachinelearning.nashequilibrium;

import java.util.Objects;

/**
 * Price decision for one strategy after an equilibrium update.
 *
 * @param key              strategy identity
 * @param targetShare      ideal volume share inside the market group
 * @param actualShare      observed volume share inside the market group
 * @param oldModifier      modifier before this update
 * @param newModifier      modifier after this update
 * @param priceMultiplier  multiplier to apply to a base price
 * @param imbalance        positive means underused/cold, negative means overused/hot
 */
public record PriceAdjustment(
        StrategyKey key,
        double targetShare,
        double actualShare,
        double oldModifier,
        double newModifier,
        double priceMultiplier,
        double imbalance
) {
    public PriceAdjustment {
        Objects.requireNonNull(key, "key");
        requireFinite(targetShare, "targetShare");
        requireFinite(actualShare, "actualShare");
        requireFinite(oldModifier, "oldModifier");
        requireFinite(newModifier, "newModifier");
        requireFinite(priceMultiplier, "priceMultiplier");
        requireFinite(imbalance, "imbalance");
    }

    public boolean isCold() {
        return imbalance > 0.0D;
    }

    public boolean isHot() {
        return imbalance < 0.0D;
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}

package com.y271727uy.shopcore.api.component.economic;

/** Stable API representation of an item's ShopCore price. */
public record PriceQuote(int baseValue, int additionalValue, int reputationValue) {
    public PriceQuote {
        if (baseValue < 0 || additionalValue < 0) {
            throw new IllegalArgumentException("price values cannot be negative");
        }
    }

    public int totalValue() {
        return Math.addExact(baseValue, additionalValue);
    }
}

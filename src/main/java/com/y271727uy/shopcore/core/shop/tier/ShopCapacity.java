package com.y271727uy.shopcore.core.shop.tier;

/**
 * Derived hard limits for a shop tier.
 */
public record ShopCapacity(
        int listingSlots,
        int stockCapacity,
        int activeCustomerLimit,
        int pendingOrderLimit
) {
    public ShopCapacity {
        if (listingSlots < 0) {
            throw new IllegalArgumentException("listingSlots cannot be negative");
        }
        if (stockCapacity < 0) {
            throw new IllegalArgumentException("stockCapacity cannot be negative");
        }
        if (activeCustomerLimit < 0) {
            throw new IllegalArgumentException("activeCustomerLimit cannot be negative");
        }
        if (pendingOrderLimit < 0) {
            throw new IllegalArgumentException("pendingOrderLimit cannot be negative");
        }
    }
}

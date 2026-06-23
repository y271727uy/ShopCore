package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.order.ShopListing;

import java.util.List;
import java.util.Objects;

public record DemandPoolSelectionResult(
        DemandPoolKey poolKey,
        List<ShopListing> listings
) {
    public DemandPoolSelectionResult {
        Objects.requireNonNull(poolKey, "poolKey");
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
    }

    public static DemandPoolSelectionResult empty(DemandPoolKey poolKey) {
        return new DemandPoolSelectionResult(poolKey, List.of());
    }
}

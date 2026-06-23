package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.order.ShopListing;

import java.util.List;
import java.util.Objects;

public record DemandPool(
        DemandPoolKey key,
        List<ShopListing> listings
) {
    public DemandPool {
        Objects.requireNonNull(key, "key");
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
    }

    public boolean isEmpty() {
        return listings.isEmpty();
    }
}

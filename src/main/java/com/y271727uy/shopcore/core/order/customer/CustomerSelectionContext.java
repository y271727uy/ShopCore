package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Objects;

public record CustomerSelectionContext(
        ShopInstance shop,
        List<ShopListing> listings,
        double reputation,
        long dayTime,
        long gameTime,
        RandomSource random
) {
    public CustomerSelectionContext {
        Objects.requireNonNull(shop, "shop");
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
        if (Double.isNaN(reputation)) {
            reputation = 0.0D;
        }
        if (dayTime < 0L) {
            throw new IllegalArgumentException("dayTime cannot be negative");
        }
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        Objects.requireNonNull(random, "random");
    }

    public boolean isDay() {
        long time = dayTime % 24_000L;
        return time >= 0L && time < 12_000L;
    }

    public boolean isNight() {
        return !isDay();
    }
}

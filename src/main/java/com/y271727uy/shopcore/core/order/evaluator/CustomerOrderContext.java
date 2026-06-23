package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.OrderSelectionContext;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Objects;

public record CustomerOrderContext(
        ShopInstance shop,
        double reputation,
        List<ShopListing> listings,
        long gameTime,
        long orderTtlTicks,
        RandomSource random
) {
    public CustomerOrderContext {
        Objects.requireNonNull(shop, "shop");
        if (Double.isNaN(reputation)) {
            reputation = 0.0D;
        }
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        if (orderTtlTicks < 0L) {
            throw new IllegalArgumentException("orderTtlTicks cannot be negative");
        }
        Objects.requireNonNull(random, "random");
    }

    public static CustomerOrderContext of(ShopInstance shop, double reputation, List<ShopListing> listings, long gameTime, RandomSource random) {
        return new CustomerOrderContext(shop, reputation, listings, gameTime, OrderSelectionContext.DEFAULT_ORDER_TTL_TICKS, random);
    }
}

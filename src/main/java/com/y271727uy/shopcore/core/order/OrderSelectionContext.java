package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Objects;

public record OrderSelectionContext(
        ShopId shopId,
        BlockPos shopPos,
        ShopTier shopTier,
        MarketTier marketTier,
        double reputation,
        List<ShopListing> listings,
        CustomerProfile customerProfile,
        long gameTime,
        long orderTtlTicks,
        RandomSource random
) {
    public static final long DEFAULT_ORDER_TTL_TICKS = 24_000L;

    public OrderSelectionContext {
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(shopPos, "shopPos");
        Objects.requireNonNull(shopTier, "shopTier");
        Objects.requireNonNull(marketTier, "marketTier");
        if (Double.isNaN(reputation)) {
            reputation = 0.0D;
        }
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
        Objects.requireNonNull(customerProfile, "customerProfile");
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        if (orderTtlTicks < 0L) {
            throw new IllegalArgumentException("orderTtlTicks cannot be negative");
        }
        Objects.requireNonNull(random, "random");
    }
}

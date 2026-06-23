package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import net.minecraft.util.RandomSource;

import java.util.Objects;

public record DemandPoolSelectionContext(
        ShopId shopId,
        ShopTier shopTier,
        MarketTier marketTier,
        long gameTime,
        RandomSource random
) {
    public DemandPoolSelectionContext {
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(shopTier, "shopTier");
        Objects.requireNonNull(marketTier, "marketTier");
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        Objects.requireNonNull(random, "random");
    }
}

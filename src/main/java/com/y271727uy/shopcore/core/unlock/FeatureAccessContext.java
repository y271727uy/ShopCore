package com.y271727uy.shopcore.core.unlock;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;

import java.util.Objects;

/**
 * Read-only values used by unlock rules.
 */
public record FeatureAccessContext(
        ShopTier shopTier,
        MarketTier marketTier,
        double reputation
) {
    public FeatureAccessContext {
        Objects.requireNonNull(shopTier, "shopTier");
        Objects.requireNonNull(marketTier, "marketTier");
        if (Double.isNaN(reputation)) {
            reputation = 0.0D;
        }
    }

    public static FeatureAccessContext of(ShopTier shopTier, MarketTier marketTier, double reputation) {
        return new FeatureAccessContext(shopTier, marketTier, reputation);
    }
}

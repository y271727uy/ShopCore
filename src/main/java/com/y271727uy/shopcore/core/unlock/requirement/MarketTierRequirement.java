package com.y271727uy.shopcore.core.unlock.requirement;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.unlock.FeatureAccessContext;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record MarketTierRequirement(MarketTier minimumTier) implements Requirement {
    public MarketTierRequirement {
        Objects.requireNonNull(minimumTier, "minimumTier");
    }

    @Override
    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "market_tier_" + minimumTier.level());
    }

    @Override
    public boolean isSatisfiedBy(FeatureAccessContext context) {
        return context != null && context.marketTier().atLeast(minimumTier);
    }
}

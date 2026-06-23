package com.y271727uy.shopcore.core.unlock.requirement;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import com.y271727uy.shopcore.core.unlock.FeatureAccessContext;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ShopTierRequirement(ShopTier minimumTier) implements Requirement {
    public ShopTierRequirement {
        Objects.requireNonNull(minimumTier, "minimumTier");
    }

    @Override
    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "shop_tier_" + minimumTier.level());
    }

    @Override
    public boolean isSatisfiedBy(FeatureAccessContext context) {
        return context != null && context.shopTier().atLeast(minimumTier);
    }
}

package com.y271727uy.shopcore.core.unlock.requirement;

import com.y271727uy.shopcore.core.unlock.FeatureAccessContext;
import net.minecraft.resources.ResourceLocation;

/**
 * Independent feature access predicate.
 */
public interface Requirement {
    ResourceLocation id();

    boolean isSatisfiedBy(FeatureAccessContext context);
}

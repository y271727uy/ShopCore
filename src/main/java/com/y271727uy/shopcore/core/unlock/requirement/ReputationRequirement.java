package com.y271727uy.shopcore.core.unlock.requirement;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.unlock.FeatureAccessContext;
import net.minecraft.resources.ResourceLocation;

public record ReputationRequirement(double minimumReputation) implements Requirement {
    public ReputationRequirement {
        if (Double.isNaN(minimumReputation) || minimumReputation < 0.0D) {
            minimumReputation = 0.0D;
        }
    }

    @Override
    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "reputation_" + Math.round(minimumReputation));
    }

    @Override
    public boolean isSatisfiedBy(FeatureAccessContext context) {
        return context != null && context.reputation() >= minimumReputation;
    }
}

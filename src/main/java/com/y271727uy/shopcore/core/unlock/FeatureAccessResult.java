package com.y271727uy.shopcore.core.unlock;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * Result of checking a feature gate.
 */
public record FeatureAccessResult(
        FeatureKey featureKey,
        boolean allowed,
        List<ResourceLocation> missingRequirements
) {
    public FeatureAccessResult {
        Objects.requireNonNull(featureKey, "featureKey");
        missingRequirements = List.copyOf(Objects.requireNonNull(missingRequirements, "missingRequirements"));
    }

    public static FeatureAccessResult allowed(FeatureKey featureKey) {
        return new FeatureAccessResult(featureKey, true, List.of());
    }

    public static FeatureAccessResult denied(FeatureKey featureKey, List<ResourceLocation> missingRequirements) {
        return new FeatureAccessResult(featureKey, false, missingRequirements);
    }
}

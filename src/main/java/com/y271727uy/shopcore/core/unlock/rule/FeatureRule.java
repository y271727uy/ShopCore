package com.y271727uy.shopcore.core.unlock.rule;

import com.y271727uy.shopcore.core.unlock.FeatureAccessContext;
import com.y271727uy.shopcore.core.unlock.FeatureAccessResult;
import com.y271727uy.shopcore.core.unlock.FeatureKey;
import com.y271727uy.shopcore.core.unlock.requirement.Requirement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * A feature gate composed from independent requirements.
 */
public record FeatureRule(FeatureKey featureKey, List<Requirement> requirements) {
    public FeatureRule {
        Objects.requireNonNull(featureKey, "featureKey");
        requirements = List.copyOf(Objects.requireNonNull(requirements, "requirements"));
    }

    public static FeatureRule of(FeatureKey featureKey, Requirement... requirements) {
        return new FeatureRule(featureKey, List.of(requirements));
    }

    public FeatureAccessResult evaluate(FeatureAccessContext context) {
        List<ResourceLocation> missing = requirements.stream()
                .filter(requirement -> !requirement.isSatisfiedBy(context))
                .map(Requirement::id)
                .toList();

        if (missing.isEmpty()) {
            return FeatureAccessResult.allowed(featureKey);
        }
        return FeatureAccessResult.denied(featureKey, missing);
    }
}

package com.y271727uy.shopcore.core.shop.tier;

import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data definition for one shop hard-capability tier.
 */
public record ShopTierConfig(
        ShopTier tier,
        ShopCapacity capacity,
        Set<ResourceLocation> operatingPolicies
) {
    public ShopTierConfig {
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(capacity, "capacity");
        operatingPolicies = Set.copyOf(Objects.requireNonNull(operatingPolicies, "operatingPolicies"));
    }

    public boolean allows(OperatingPolicyKey policyKey) {
        return policyKey != null && operatingPolicies.contains(policyKey.id());
    }

    public static ShopTierConfig of(ShopTier tier, ShopCapacity capacity, Set<OperatingPolicyKey> policies) {
        Objects.requireNonNull(policies, "policies");
        return new ShopTierConfig(
                tier,
                capacity,
                policies.stream().map(OperatingPolicyKey::id).collect(Collectors.toUnmodifiableSet())
        );
    }
}

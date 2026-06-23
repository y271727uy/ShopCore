package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.market.demand.CustomerTypeKey;
import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Demand-side customer data. This is not NPC movement/behavior.
 */
public record CustomerProfile(
        ResourceLocation customerType,
        Set<ResourceLocation> acceptedDemandCategories,
        Set<ResourceLocation> preferredDemandCategories,
        Set<ResourceLocation> excludedDemandCategories,
        Set<OrderComplexity> acceptedComplexities,
        Set<OrderComplexity> preferredComplexities,
        int minBudget,
        int maxBudget,
        int minQuantity,
        int maxQuantity,
        double priceSensitivity
) {
    public CustomerProfile {
        Objects.requireNonNull(customerType, "customerType");
        acceptedDemandCategories = Set.copyOf(Objects.requireNonNull(acceptedDemandCategories, "acceptedDemandCategories"));
        preferredDemandCategories = Set.copyOf(Objects.requireNonNull(preferredDemandCategories, "preferredDemandCategories"));
        excludedDemandCategories = Set.copyOf(Objects.requireNonNull(excludedDemandCategories, "excludedDemandCategories"));
        acceptedComplexities = Set.copyOf(Objects.requireNonNull(acceptedComplexities, "acceptedComplexities"));
        preferredComplexities = Set.copyOf(Objects.requireNonNull(preferredComplexities, "preferredComplexities"));
        if (minBudget < 0) {
            throw new IllegalArgumentException("minBudget cannot be negative");
        }
        if (maxBudget < minBudget) {
            throw new IllegalArgumentException("maxBudget cannot be lower than minBudget");
        }
        if (minQuantity < 1) {
            throw new IllegalArgumentException("minQuantity must be at least 1");
        }
        if (maxQuantity < minQuantity) {
            throw new IllegalArgumentException("maxQuantity cannot be lower than minQuantity");
        }
        if (!Double.isFinite(priceSensitivity) || priceSensitivity < 0.0D) {
            throw new IllegalArgumentException("priceSensitivity must be a finite non-negative value");
        }
    }

    public static CustomerProfile of(
            CustomerTypeKey customerType,
            Set<DemandCategoryKey> acceptedDemandCategories,
            Set<DemandCategoryKey> preferredDemandCategories,
            Set<DemandCategoryKey> excludedDemandCategories,
            Set<OrderComplexity> acceptedComplexities,
            Set<OrderComplexity> preferredComplexities,
            int minBudget,
            int maxBudget,
            int minQuantity,
            int maxQuantity,
            double priceSensitivity
    ) {
        return new CustomerProfile(
                customerType.id(),
                ids(acceptedDemandCategories),
                ids(preferredDemandCategories),
                ids(excludedDemandCategories),
                acceptedComplexities,
                preferredComplexities,
                minBudget,
                maxBudget,
                minQuantity,
                maxQuantity,
                priceSensitivity
        );
    }

    public boolean acceptsCategory(ResourceLocation category) {
        return category != null
                && !excludedDemandCategories.contains(category)
                && (acceptedDemandCategories.isEmpty() || acceptedDemandCategories.contains(category));
    }

    public boolean prefersCategory(ResourceLocation category) {
        return category != null && preferredDemandCategories.contains(category);
    }

    public boolean acceptsComplexity(OrderComplexity complexity) {
        return complexity != null && (acceptedComplexities.isEmpty() || acceptedComplexities.contains(complexity));
    }

    public boolean prefersComplexity(OrderComplexity complexity) {
        return complexity != null && preferredComplexities.contains(complexity);
    }

    private static Set<ResourceLocation> ids(Set<DemandCategoryKey> keys) {
        return Objects.requireNonNull(keys, "keys").stream()
                .map(DemandCategoryKey::id)
                .collect(Collectors.toUnmodifiableSet());
    }
}

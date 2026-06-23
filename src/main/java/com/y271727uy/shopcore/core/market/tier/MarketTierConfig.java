package com.y271727uy.shopcore.core.market.tier;

import com.y271727uy.shopcore.core.market.demand.CustomerTypeKey;
import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data definition for one market demand tier.
 */
public record MarketTierConfig(
        MarketTier tier,
        MarketDemandProfile demandProfile
) {
    public MarketTierConfig {
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(demandProfile, "demandProfile");
    }

    public boolean allowsDemandCategory(DemandCategoryKey categoryKey) {
        return categoryKey != null && demandProfile.demandCategories().contains(categoryKey.id());
    }

    public boolean allowsCustomerType(CustomerTypeKey customerTypeKey) {
        return customerTypeKey != null && demandProfile.customerTypes().contains(customerTypeKey.id());
    }

    public boolean allowsComplexity(OrderComplexity complexity) {
        return demandProfile.allows(complexity);
    }

    public static MarketTierConfig of(
            MarketTier tier,
            Set<DemandCategoryKey> categories,
            Set<CustomerTypeKey> customerTypes,
            Set<OrderComplexity> complexities,
            int maxOrderLines,
            int maxRequestedCount
    ) {
        Objects.requireNonNull(categories, "categories");
        Objects.requireNonNull(customerTypes, "customerTypes");
        return new MarketTierConfig(
                tier,
                new MarketDemandProfile(
                        categories.stream().map(DemandCategoryKey::id).collect(Collectors.toUnmodifiableSet()),
                        customerTypes.stream().map(CustomerTypeKey::id).collect(Collectors.toUnmodifiableSet()),
                        Set.copyOf(complexities),
                        maxOrderLines,
                        maxRequestedCount
                )
        );
    }
}

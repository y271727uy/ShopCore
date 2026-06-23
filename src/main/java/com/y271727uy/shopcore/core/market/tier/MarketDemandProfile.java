package com.y271727uy.shopcore.core.market.tier;

import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

/**
 * Derived demand limits for a market tier.
 */
public record MarketDemandProfile(
        Set<ResourceLocation> demandCategories,
        Set<ResourceLocation> customerTypes,
        Set<OrderComplexity> orderComplexities,
        int maxOrderLines,
        int maxRequestedCount
) {
    public MarketDemandProfile {
        demandCategories = Set.copyOf(Objects.requireNonNull(demandCategories, "demandCategories"));
        customerTypes = Set.copyOf(Objects.requireNonNull(customerTypes, "customerTypes"));
        orderComplexities = Set.copyOf(Objects.requireNonNull(orderComplexities, "orderComplexities"));
        if (maxOrderLines < 1) {
            throw new IllegalArgumentException("maxOrderLines must be at least 1");
        }
        if (maxRequestedCount < 1) {
            throw new IllegalArgumentException("maxRequestedCount must be at least 1");
        }
    }

    public boolean allows(OrderComplexity complexity) {
        return complexity != null && orderComplexities.contains(complexity);
    }
}

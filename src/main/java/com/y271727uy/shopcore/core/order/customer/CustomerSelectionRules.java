package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;

public final class CustomerSelectionRules {
    private CustomerSelectionRules() {
    }

    public static CustomerSelectionRule acceptsAnyAvailableListing() {
        return (context, profile) -> {
            for (ShopListing listing : context.listings()) {
                if (listing.enabled()
                        && profile.acceptsCategory(listing.demandCategory())
                        && profile.acceptsComplexity(listing.complexity())) {
                    return true;
                }
            }
            return false;
        };
    }

    public static CustomerSelectionRule acceptsCategory(ResourceLocation category) {
        return (context, profile) -> profile.acceptsCategory(category);
    }

    public static CustomerSelectionRule acceptsComplexity(OrderComplexity complexity) {
        return (context, profile) -> profile.acceptsComplexity(complexity);
    }

    public static CustomerSelectionRule dayOnly() {
        return (context, profile) -> context.isDay();
    }

    public static CustomerSelectionRule nightOnly() {
        return (context, profile) -> context.isNight();
    }

    public static CustomerSelectionRule reputationAtLeast(double minimum) {
        return (context, profile) -> context.reputation() >= minimum;
    }

    public static CustomerSelectionRule customerType(ResourceLocation customerType) {
        return (context, profile) -> profile.customerType().equals(customerType);
    }
}

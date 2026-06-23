package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;

public final class CustomerWeightRules {
    private CustomerWeightRules() {
    }

    public static CustomerWeightRule constant(double weight) {
        return (context, profile) -> Math.max(0.0D, weight);
    }

    public static CustomerWeightRule preferredListingMultiplier(double multiplier) {
        return (context, profile) -> {
            for (ShopListing listing : context.listings()) {
                if (profile.prefersCategory(listing.demandCategory()) || profile.prefersComplexity(listing.complexity())) {
                    return Math.max(0.0D, multiplier);
                }
            }
            return 1.0D;
        };
    }

    public static CustomerWeightRule categoryMultiplier(ResourceLocation category, double multiplier) {
        return (context, profile) -> profile.acceptsCategory(category) ? Math.max(0.0D, multiplier) : 1.0D;
    }

    public static CustomerWeightRule complexityMultiplier(OrderComplexity complexity, double multiplier) {
        return (context, profile) -> profile.acceptsComplexity(complexity) ? Math.max(0.0D, multiplier) : 1.0D;
    }

    public static CustomerWeightRule reputationMultiplier(double reputationStep, double multiplierPerStep) {
        return (context, profile) -> {
            if (reputationStep <= 0.0D || multiplierPerStep <= 0.0D) {
                return 1.0D;
            }
            double steps = Math.max(0.0D, context.reputation()) / reputationStep;
            return Math.pow(multiplierPerStep, steps);
        };
    }

    public static CustomerWeightRule dayMultiplier(double multiplier) {
        return (context, profile) -> context.isDay() ? Math.max(0.0D, multiplier) : 1.0D;
    }

    public static CustomerWeightRule nightMultiplier(double multiplier) {
        return (context, profile) -> context.isNight() ? Math.max(0.0D, multiplier) : 1.0D;
    }
}

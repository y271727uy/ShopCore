package com.y271727uy.shopcore.core.shop.opening.rule;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.shop.opening.AbstractShopOpeningRule;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningContext;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRules;

import java.util.Objects;
import java.util.function.Predicate;

public class MinStageEffectiveListingCountRule extends AbstractShopOpeningRule {
    private final int minCount;
    private final Predicate<ShopListing> stageEffectivePredicate;

    public MinStageEffectiveListingCountRule(int minCount, Predicate<ShopListing> stageEffectivePredicate) {
        super(ShopOpeningRules.MIN_STAGE_EFFECTIVE_LISTING_COUNT, ShopOpeningRules.REASON_NOT_ENOUGH_STAGE_EFFECTIVE_LISTINGS);
        if (minCount < 0) {
            throw new IllegalArgumentException("minCount cannot be negative");
        }
        this.minCount = minCount;
        this.stageEffectivePredicate = Objects.requireNonNull(stageEffectivePredicate, "stageEffectivePredicate");
    }

    @Override
    public ShopOpeningResult validate(ShopOpeningContext context) {
        int count = 0;
        for (ShopListing listing : context.listings()) {
            if (listing.enabled() && stageEffectivePredicate.test(listing)) {
                count++;
            }
        }
        return count >= minCount ? pass() : fail(minCount, count);
    }
}

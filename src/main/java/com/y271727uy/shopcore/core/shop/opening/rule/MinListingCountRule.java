package com.y271727uy.shopcore.core.shop.opening.rule;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.shop.opening.AbstractShopOpeningRule;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningContext;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRules;
import net.minecraft.resources.ResourceLocation;

public class MinListingCountRule extends AbstractShopOpeningRule {
    private final int minCount;

    public MinListingCountRule(int minCount) {
        this(ShopOpeningRules.MIN_LISTING_COUNT, minCount);
    }

    protected MinListingCountRule(ResourceLocation ruleId, int minCount) {
        super(ruleId, ShopOpeningRules.REASON_NOT_ENOUGH_LISTINGS);
        if (minCount < 0) {
            throw new IllegalArgumentException("minCount cannot be negative");
        }
        this.minCount = minCount;
    }

    @Override
    public ShopOpeningResult validate(ShopOpeningContext context) {
        int count = 0;
        for (ShopListing listing : context.listings()) {
            if (listing.enabled()) {
                count++;
            }
        }
        return count >= minCount ? pass() : fail(minCount, count);
    }
}

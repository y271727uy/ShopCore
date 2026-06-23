package com.y271727uy.shopcore.core.shop.opening.rule;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.menu.MenuItemCandidates;
import com.y271727uy.shopcore.core.shop.opening.AbstractShopOpeningRule;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningContext;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRules;
import com.y271727uy.shopcore.core.shop.opening.tag.ShopOpeningItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MinNonSauceListingCountRule extends AbstractShopOpeningRule {
    private final int minCount;

    public MinNonSauceListingCountRule(int minCount) {
        super(ShopOpeningRules.MIN_NON_SAUCE_LISTING_COUNT, ShopOpeningRules.REASON_NOT_ENOUGH_NON_SAUCE_LISTINGS);
        if (minCount < 0) {
            throw new IllegalArgumentException("minCount cannot be negative");
        }
        this.minCount = minCount;
    }

    @Override
    public ShopOpeningResult validate(ShopOpeningContext context) {
        int count = 0;
        for (ShopListing listing : context.listings()) {
            if (listing.enabled() && hasNonSauceCandidate(listing)) {
                count++;
            }
        }
        return count >= minCount ? pass() : fail(minCount, count);
    }

    protected boolean hasNonSauceCandidate(ShopListing listing) {
        List<ItemStack> candidates;
        try {
            candidates = MenuItemCandidates.resolve(listing.menuId());
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        for (ItemStack candidate : candidates) {
            if (!candidate.isEmpty() && !candidate.is(ShopOpeningItemTags.SAUCE)) {
                return true;
            }
        }
        return false;
    }
}

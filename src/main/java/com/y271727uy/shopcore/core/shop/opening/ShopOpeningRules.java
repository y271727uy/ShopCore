package com.y271727uy.shopcore.core.shop.opening;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;

public final class ShopOpeningRules {
    public static final ResourceLocation MIN_LISTING_COUNT = shopcore("min_listing_count");
    public static final ResourceLocation MIN_NON_SAUCE_LISTING_COUNT = shopcore("min_non_sauce_listing_count");
    public static final ResourceLocation MIN_STAGE_EFFECTIVE_LISTING_COUNT = shopcore("min_stage_effective_listing_count");

    public static final ResourceLocation REASON_NOT_ENOUGH_LISTINGS = shopcore("not_enough_listings");
    public static final ResourceLocation REASON_NOT_ENOUGH_NON_SAUCE_LISTINGS = shopcore("not_enough_non_sauce_listings");
    public static final ResourceLocation REASON_NOT_ENOUGH_STAGE_EFFECTIVE_LISTINGS = shopcore("not_enough_stage_effective_listings");

    private ShopOpeningRules() {
    }

    public static ResourceLocation shopcore(String path) {
        return ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path);
    }
}

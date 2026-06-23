package com.y271727uy.shopcore.core.shop.opening;

import net.minecraft.resources.ResourceLocation;

public interface ShopOpeningRule {
    ResourceLocation ruleId();

    ShopOpeningResult validate(ShopOpeningContext context);
}

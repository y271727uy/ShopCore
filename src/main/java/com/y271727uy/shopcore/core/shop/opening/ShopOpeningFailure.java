package com.y271727uy.shopcore.core.shop.opening;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ShopOpeningFailure(
        ResourceLocation ruleId,
        ResourceLocation reasonKey,
        int requiredCount,
        int actualCount
) {
    public ShopOpeningFailure {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(reasonKey, "reasonKey");
        if (requiredCount < 0) {
            throw new IllegalArgumentException("requiredCount cannot be negative");
        }
        if (actualCount < 0) {
            throw new IllegalArgumentException("actualCount cannot be negative");
        }
    }
}

package com.y271727uy.shopcore.core.shop.opening;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public abstract class AbstractShopOpeningRule implements ShopOpeningRule {
    private final ResourceLocation ruleId;
    private final ResourceLocation reasonKey;

    protected AbstractShopOpeningRule(ResourceLocation ruleId, ResourceLocation reasonKey) {
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.reasonKey = Objects.requireNonNull(reasonKey, "reasonKey");
    }

    @Override
    public final ResourceLocation ruleId() {
        return ruleId;
    }

    protected final ResourceLocation reasonKey() {
        return reasonKey;
    }

    protected final ShopOpeningResult pass() {
        return ShopOpeningResult.allowed();
    }

    protected final ShopOpeningResult fail(int requiredCount, int actualCount) {
        return ShopOpeningResult.failed(new ShopOpeningFailure(ruleId, reasonKey, requiredCount, actualCount));
    }
}

package com.y271727uy.shopcore.core.shop.opening;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public record ShopOpeningRuleSet(
        ResourceLocation ruleSetId,
        List<ShopOpeningRule> rules
) {
    public ShopOpeningRuleSet {
        Objects.requireNonNull(ruleSetId, "ruleSetId");
        rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    public static ShopOpeningRuleSet of(ResourceLocation ruleSetId, List<ShopOpeningRule> rules) {
        return new ShopOpeningRuleSet(ruleSetId, rules);
    }
}

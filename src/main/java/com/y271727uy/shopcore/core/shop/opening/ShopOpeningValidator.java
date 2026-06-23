package com.y271727uy.shopcore.core.shop.opening;

import java.util.List;
import java.util.Objects;

public final class ShopOpeningValidator {
    private ShopOpeningValidator() {
    }

    public static ShopOpeningResult validate(ShopOpeningContext context, ShopOpeningRuleSet ruleSet) {
        Objects.requireNonNull(ruleSet, "ruleSet");
        return validate(context, ruleSet.rules());
    }

    public static ShopOpeningResult validate(ShopOpeningContext context, List<ShopOpeningRule> rules) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(rules, "rules");

        ShopOpeningResult result = ShopOpeningResult.allowed();
        for (ShopOpeningRule rule : rules) {
            result = result.merge(rule.validate(context));
        }
        return result;
    }
}

package com.y271727uy.shopcore.core.unlock;

import com.y271727uy.shopcore.core.unlock.rule.FeatureRule;
import com.y271727uy.shopcore.core.unlock.rule.FeatureRuleRegistry;

import java.util.List;

/**
 * Single entrypoint for feature access checks.
 */
public final class FeatureAccessService {
    private FeatureAccessService() {
    }

    public static FeatureAccessResult canUse(FeatureAccessContext context, FeatureKey featureKey) {
        FeatureRule rule = FeatureRuleRegistry.get(featureKey);
        if (rule == null) {
            return FeatureAccessResult.denied(featureKey, List.of(featureKey.id()));
        }
        return rule.evaluate(context);
    }
}

package com.y271727uy.shopcore.core.unlock.rule;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import com.y271727uy.shopcore.core.unlock.FeatureKey;
import com.y271727uy.shopcore.core.unlock.requirement.MarketTierRequirement;
import com.y271727uy.shopcore.core.unlock.requirement.ReputationRequirement;
import com.y271727uy.shopcore.core.unlock.requirement.ShopTierRequirement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable feature rule registry. Defaults are conservative and can be replaced by bootstrap code.
 */
public final class FeatureRuleRegistry {
    private static final Map<FeatureKey, FeatureRule> RULES = new LinkedHashMap<>();

    static {
        resetDefaults();
    }

    private FeatureRuleRegistry() {
    }

    public static synchronized void resetDefaults() {
        RULES.clear();
        register(FeatureRule.of(
                FeatureKey.NIGHT_OPERATING_POLICY,
                new ShopTierRequirement(ShopTier.TIER_2),
                new ReputationRequirement(200.0D)
        ));
        register(FeatureRule.of(
                FeatureKey.ALWAYS_OPEN_POLICY,
                new ShopTierRequirement(ShopTier.TIER_3),
                new ReputationRequirement(350.0D)
        ));
        register(FeatureRule.of(
                FeatureKey.QUALITY_ORDERS,
                new MarketTierRequirement(MarketTier.TIER_2),
                new ReputationRequirement(150.0D)
        ));
        register(FeatureRule.of(
                FeatureKey.MULTI_LINE_ORDERS,
                new MarketTierRequirement(MarketTier.TIER_2),
                new ReputationRequirement(250.0D)
        ));
        register(FeatureRule.of(
                FeatureKey.TIMED_ORDERS,
                new MarketTierRequirement(MarketTier.TIER_3),
                new ReputationRequirement(400.0D)
        ));
        register(FeatureRule.of(
                FeatureKey.BULK_ORDERS,
                new MarketTierRequirement(MarketTier.TIER_3),
                new ReputationRequirement(500.0D)
        ));
        register(FeatureRule.of(
                FeatureKey.RARE_CUSTOMERS,
                new MarketTierRequirement(MarketTier.TIER_4),
                new ReputationRequirement(800.0D)
        ));
    }

    public static synchronized void register(FeatureRule rule) {
        Objects.requireNonNull(rule, "rule");
        RULES.put(rule.featureKey(), rule);
    }

    public static synchronized FeatureRule get(FeatureKey featureKey) {
        return RULES.get(featureKey);
    }

    public static synchronized Map<FeatureKey, FeatureRule> snapshot() {
        return Map.copyOf(RULES);
    }
}

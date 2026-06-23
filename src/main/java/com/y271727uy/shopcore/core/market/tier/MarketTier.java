package com.y271727uy.shopcore.core.market.tier;

/**
 * Demand-side market access tier.
 */
public enum MarketTier {
    TIER_1(1),
    TIER_2(2),
    TIER_3(3),
    TIER_4(4),
    TIER_5(5);

    private final int level;

    MarketTier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public boolean atLeast(MarketTier other) {
        return this.level >= other.level;
    }

    public static MarketTier fromLevel(int level) {
        if (level <= 1) {
            return TIER_1;
        }
        if (level >= 5) {
            return TIER_5;
        }
        return values()[level - 1];
    }
}

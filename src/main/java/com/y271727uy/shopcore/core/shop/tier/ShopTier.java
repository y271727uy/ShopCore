package com.y271727uy.shopcore.core.shop.tier;

/**
 * Hard-capability tier for a concrete shop instance.
 */
public enum ShopTier {
    TIER_1(1),
    TIER_2(2),
    TIER_3(3),
    TIER_4(4),
    TIER_5(5);

    private final int level;

    ShopTier(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public boolean atLeast(ShopTier other) {
        return this.level >= other.level;
    }

    public static ShopTier fromLevel(int level) {
        if (level <= 1) {
            return TIER_1;
        }
        if (level >= 5) {
            return TIER_5;
        }
        return values()[level - 1];
    }
}

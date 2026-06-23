package com.y271727uy.shopcore.core.shop.tier;

import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyKey;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default shop tier table. Gameplay code should query this instead of hard-coding tier numbers.
 */
public final class ShopTierConfigs {
    private static final Map<ShopTier, ShopTierConfig> DEFAULTS = createDefaults();

    private ShopTierConfigs() {
    }

    public static ShopTierConfig get(ShopTier tier) {
        return DEFAULTS.get(Objects.requireNonNull(tier, "tier"));
    }

    public static Map<ShopTier, ShopTierConfig> defaults() {
        return Map.copyOf(DEFAULTS);
    }

    private static Map<ShopTier, ShopTierConfig> createDefaults() {
        EnumMap<ShopTier, ShopTierConfig> map = new EnumMap<>(ShopTier.class);
        map.put(ShopTier.TIER_1, ShopTierConfig.of(
                ShopTier.TIER_1,
                new ShopCapacity(3, 9, 1, 2),
                Set.of(OperatingPolicyKey.MANUAL, OperatingPolicyKey.DAY_OPEN)
        ));
        map.put(ShopTier.TIER_2, ShopTierConfig.of(
                ShopTier.TIER_2,
                new ShopCapacity(6, 18, 2, 4),
                Set.of(OperatingPolicyKey.MANUAL, OperatingPolicyKey.DAY_OPEN, OperatingPolicyKey.NIGHT_OPEN)
        ));
        map.put(ShopTier.TIER_3, ShopTierConfig.of(
                ShopTier.TIER_3,
                new ShopCapacity(9, 27, 3, 6),
                Set.of(OperatingPolicyKey.MANUAL, OperatingPolicyKey.DAY_OPEN, OperatingPolicyKey.NIGHT_OPEN, OperatingPolicyKey.ALWAYS_OPEN)
        ));
        map.put(ShopTier.TIER_4, ShopTierConfig.of(
                ShopTier.TIER_4,
                new ShopCapacity(12, 36, 4, 9),
                Set.of(OperatingPolicyKey.MANUAL, OperatingPolicyKey.DAY_OPEN, OperatingPolicyKey.NIGHT_OPEN, OperatingPolicyKey.ALWAYS_OPEN)
        ));
        map.put(ShopTier.TIER_5, ShopTierConfig.of(
                ShopTier.TIER_5,
                new ShopCapacity(18, 54, 6, 12),
                Set.of(OperatingPolicyKey.MANUAL, OperatingPolicyKey.DAY_OPEN, OperatingPolicyKey.NIGHT_OPEN, OperatingPolicyKey.ALWAYS_OPEN)
        ));
        return Map.copyOf(map);
    }
}

package com.y271727uy.shopcore.core.market.tier;

import com.y271727uy.shopcore.core.market.demand.CustomerTypeKey;
import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default market demand tier table. Demand generation should query this instead of hard-coding tier numbers.
 */
public final class MarketTierConfigs {
    private static final Map<MarketTier, MarketTierConfig> DEFAULTS = createDefaults();

    private MarketTierConfigs() {
    }

    public static MarketTierConfig get(MarketTier tier) {
        return DEFAULTS.get(Objects.requireNonNull(tier, "tier"));
    }

    public static Map<MarketTier, MarketTierConfig> defaults() {
        return Map.copyOf(DEFAULTS);
    }

    private static Map<MarketTier, MarketTierConfig> createDefaults() {
        EnumMap<MarketTier, MarketTierConfig> map = new EnumMap<>(MarketTier.class);
        map.put(MarketTier.TIER_1, MarketTierConfig.of(
                MarketTier.TIER_1,
                Set.of(DemandCategoryKey.BASIC_GOODS),
                Set.of(CustomerTypeKey.COMMON),
                Set.of(OrderComplexity.SINGLE_ITEM),
                1,
                16
        ));
        map.put(MarketTier.TIER_2, MarketTierConfig.of(
                MarketTier.TIER_2,
                Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS, DemandCategoryKey.QUALITY_GOODS),
                Set.of(CustomerTypeKey.COMMON, CustomerTypeKey.PICKY),
                Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.QUALITY_ITEM, OrderComplexity.MULTI_LINE),
                2,
                32
        ));
        map.put(MarketTier.TIER_3, MarketTierConfig.of(
                MarketTier.TIER_3,
                Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS, DemandCategoryKey.QUALITY_GOODS, DemandCategoryKey.BULK_GOODS),
                Set.of(CustomerTypeKey.COMMON, CustomerTypeKey.PICKY, CustomerTypeKey.BULK_BUYER),
                Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.QUALITY_ITEM, OrderComplexity.MULTI_LINE, OrderComplexity.TIMED, OrderComplexity.BULK),
                3,
                64
        ));
        map.put(MarketTier.TIER_4, MarketTierConfig.of(
                MarketTier.TIER_4,
                Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS, DemandCategoryKey.QUALITY_GOODS, DemandCategoryKey.BULK_GOODS, DemandCategoryKey.RARE_GOODS),
                Set.of(CustomerTypeKey.COMMON, CustomerTypeKey.PICKY, CustomerTypeKey.BULK_BUYER, CustomerTypeKey.RARE),
                Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.QUALITY_ITEM, OrderComplexity.MULTI_LINE, OrderComplexity.TIMED, OrderComplexity.BULK, OrderComplexity.RARE),
                4,
                96
        ));
        map.put(MarketTier.TIER_5, MarketTierConfig.of(
                MarketTier.TIER_5,
                Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS, DemandCategoryKey.QUALITY_GOODS, DemandCategoryKey.BULK_GOODS, DemandCategoryKey.RARE_GOODS),
                Set.of(CustomerTypeKey.COMMON, CustomerTypeKey.PICKY, CustomerTypeKey.BULK_BUYER, CustomerTypeKey.RARE),
                Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.QUALITY_ITEM, OrderComplexity.MULTI_LINE, OrderComplexity.TIMED, OrderComplexity.BULK, OrderComplexity.RARE),
                5,
                128
        ));
        return Map.copyOf(map);
    }
}

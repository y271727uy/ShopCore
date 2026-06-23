package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.market.demand.CustomerTypeKey;
import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;

import java.util.List;
import java.util.Set;

/**
 * Conservative default customer profiles used by order generation.
 */
public final class CustomerProfiles {
    public static final CustomerProfile COMMON = CustomerProfile.of(
            CustomerTypeKey.COMMON,
            Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS),
            Set.of(DemandCategoryKey.BASIC_GOODS),
            Set.of(),
            Set.of(OrderComplexity.SINGLE_ITEM),
            Set.of(OrderComplexity.SINGLE_ITEM),
            1,
            96,
            1,
            16,
            0.55D
    );

    public static final CustomerProfile PICKY = CustomerProfile.of(
            CustomerTypeKey.PICKY,
            Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS, DemandCategoryKey.QUALITY_GOODS),
            Set.of(DemandCategoryKey.QUALITY_GOODS),
            Set.of(),
            Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.QUALITY_ITEM, OrderComplexity.MULTI_LINE),
            Set.of(OrderComplexity.QUALITY_ITEM),
            16,
            256,
            1,
            24,
            0.35D
    );

    public static final CustomerProfile BULK_BUYER = CustomerProfile.of(
            CustomerTypeKey.BULK_BUYER,
            Set.of(DemandCategoryKey.BASIC_GOODS, DemandCategoryKey.PROCESSED_GOODS, DemandCategoryKey.BULK_GOODS),
            Set.of(DemandCategoryKey.BULK_GOODS),
            Set.of(),
            Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.BULK),
            Set.of(OrderComplexity.BULK),
            64,
            1024,
            16,
            128,
            0.25D
    );

    public static final CustomerProfile RARE = CustomerProfile.of(
            CustomerTypeKey.RARE,
            Set.of(DemandCategoryKey.QUALITY_GOODS, DemandCategoryKey.BULK_GOODS, DemandCategoryKey.RARE_GOODS),
            Set.of(DemandCategoryKey.RARE_GOODS),
            Set.of(),
            Set.of(OrderComplexity.SINGLE_ITEM, OrderComplexity.QUALITY_ITEM, OrderComplexity.MULTI_LINE, OrderComplexity.TIMED, OrderComplexity.BULK, OrderComplexity.RARE),
            Set.of(OrderComplexity.RARE, OrderComplexity.MULTI_LINE),
            128,
            4096,
            1,
            64,
            0.15D
    );

    private CustomerProfiles() {
    }

    public static List<CustomerProfile> defaults() {
        return List.of(COMMON, PICKY, BULK_BUYER, RARE);
    }
}

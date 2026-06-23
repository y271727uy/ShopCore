package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import com.y271727uy.shopcore.core.order.PricingMode;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.economic.price.PriceRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Objects;

public final class TagBackedDemandPools {
    private TagBackedDemandPools() {
    }

    public static DemandPool create(
            DemandPoolKey poolKey,
            String tagId,
            DemandCategoryKey demandCategory,
            OrderComplexity complexity,
            int maxPerOrder,
            int stockCount
    ) {
        return create(poolKey, PriceRegistry.resolveItemTag(tagId), demandCategory, complexity, maxPerOrder, stockCount);
    }

    public static DemandPool create(
            DemandPoolKey poolKey,
            TagKey<Item> itemTag,
            DemandCategoryKey demandCategory,
            OrderComplexity complexity,
            int maxPerOrder,
            int stockCount
    ) {
        Objects.requireNonNull(poolKey, "poolKey");
        Objects.requireNonNull(itemTag, "itemTag");
        Objects.requireNonNull(demandCategory, "demandCategory");
        Objects.requireNonNull(complexity, "complexity");
        String tagMenuId = "#" + itemTag.location();
        ShopListing listing = new ShopListing(
                0,
                tagMenuId,
                demandCategory.id(),
                complexity,
                true,
                PricingMode.AUTO,
                0,
                maxPerOrder,
                stockCount
        );
        return new DemandPool(poolKey, List.of(listing));
    }
}

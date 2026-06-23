package com.y271727uy.shopcore.core.market.demand;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Stable identifier for item demand pools. These are market concepts, not recipe locks.
 */
public record DemandCategoryKey(ResourceLocation id) {
    public static final DemandCategoryKey BASIC_GOODS = shopcore("basic_goods");
    public static final DemandCategoryKey PROCESSED_GOODS = shopcore("processed_goods");
    public static final DemandCategoryKey QUALITY_GOODS = shopcore("quality_goods");
    public static final DemandCategoryKey BULK_GOODS = shopcore("bulk_goods");
    public static final DemandCategoryKey RARE_GOODS = shopcore("rare_goods");

    public DemandCategoryKey {
        Objects.requireNonNull(id, "id");
    }

    public static DemandCategoryKey shopcore(String path) {
        return new DemandCategoryKey(ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }

    public static DemandCategoryKey of(ResourceLocation id) {
        return new DemandCategoryKey(id);
    }
}

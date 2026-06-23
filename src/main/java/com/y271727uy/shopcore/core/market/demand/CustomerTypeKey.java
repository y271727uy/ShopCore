package com.y271727uy.shopcore.core.market.demand;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Stable identifier for a customer pool/type.
 */
public record CustomerTypeKey(ResourceLocation id) {
    public static final CustomerTypeKey COMMON = shopcore("common");
    public static final CustomerTypeKey PICKY = shopcore("picky");
    public static final CustomerTypeKey BULK_BUYER = shopcore("bulk_buyer");
    public static final CustomerTypeKey RARE = shopcore("rare");

    public CustomerTypeKey {
        Objects.requireNonNull(id, "id");
    }

    public static CustomerTypeKey shopcore(String path) {
        return new CustomerTypeKey(ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }

    public static CustomerTypeKey of(ResourceLocation id) {
        return new CustomerTypeKey(id);
    }
}

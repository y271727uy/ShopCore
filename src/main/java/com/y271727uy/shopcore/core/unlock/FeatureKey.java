package com.y271727uy.shopcore.core.unlock;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Stable identifier for a gated capability.
 */
public record FeatureKey(ResourceLocation id) {
    public static final FeatureKey NIGHT_OPERATING_POLICY = shopcore("night_operating_policy");
    public static final FeatureKey ALWAYS_OPEN_POLICY = shopcore("always_open_policy");
    public static final FeatureKey QUALITY_ORDERS = shopcore("quality_orders");
    public static final FeatureKey MULTI_LINE_ORDERS = shopcore("multi_line_orders");
    public static final FeatureKey TIMED_ORDERS = shopcore("timed_orders");
    public static final FeatureKey BULK_ORDERS = shopcore("bulk_orders");
    public static final FeatureKey RARE_CUSTOMERS = shopcore("rare_customers");

    public FeatureKey {
        Objects.requireNonNull(id, "id");
    }

    public static FeatureKey shopcore(String path) {
        return new FeatureKey(ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }

    public static FeatureKey of(ResourceLocation id) {
        return new FeatureKey(id);
    }
}

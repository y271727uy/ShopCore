package com.y271727uy.shopcore.core.shop.policy;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Stable identifier for a shop operating policy.
 */
public record OperatingPolicyKey(ResourceLocation id) {
    public static final OperatingPolicyKey MANUAL = shopcore("manual");
    public static final OperatingPolicyKey DAY_OPEN = shopcore("day_open");
    public static final OperatingPolicyKey NIGHT_OPEN = shopcore("night_open");
    public static final OperatingPolicyKey ALWAYS_OPEN = shopcore("always_open");

    public OperatingPolicyKey {
        Objects.requireNonNull(id, "id");
    }

    public static OperatingPolicyKey shopcore(String path) {
        return new OperatingPolicyKey(ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }

    public static OperatingPolicyKey of(ResourceLocation id) {
        return new OperatingPolicyKey(id);
    }
}

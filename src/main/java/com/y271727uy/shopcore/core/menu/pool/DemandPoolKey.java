package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record DemandPoolKey(ResourceLocation id) {
    public DemandPoolKey {
        Objects.requireNonNull(id, "id");
    }

    public static DemandPoolKey shopcore(String path) {
        return new DemandPoolKey(ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }

    public static DemandPoolKey of(ResourceLocation id) {
        return new DemandPoolKey(id);
    }
}

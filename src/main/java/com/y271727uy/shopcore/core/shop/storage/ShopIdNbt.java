package com.y271727uy.shopcore.core.shop.storage;

import com.y271727uy.shopcore.core.shop.instance.ShopId;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public final class ShopIdNbt {
    private ShopIdNbt() {
    }

    public static void put(CompoundTag tag, String key, ShopId shopId) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(shopId, "shopId");
        tag.putUUID(key, shopId.value());
    }

    public static ShopId get(CompoundTag tag, String key, ShopId fallback) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(fallback, "fallback");
        if (!tag.hasUUID(key)) {
            return fallback;
        }
        return ShopId.of(tag.getUUID(key));
    }
}

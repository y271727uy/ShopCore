package com.y271727uy.shopcore.core.shop.instance;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable identity for one concrete shop instance.
 * This is intentionally independent from player identity because one player can own multiple shops.
 */
public record ShopId(UUID value) {
    public ShopId {
        Objects.requireNonNull(value, "value");
    }

    public static ShopId random() {
        return new ShopId(UUID.randomUUID());
    }

    public static ShopId of(UUID value) {
        return new ShopId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

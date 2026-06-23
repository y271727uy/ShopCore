package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.Objects;
import java.util.Optional;

public interface ShopMenuRepository {
    Optional<ShopMenuSnapshot> find(ShopId shopId);

    ShopMenuSnapshot save(ShopMenuSnapshot snapshot);

    boolean delete(ShopId shopId);

    default ShopMenuSnapshot getOrCreate(ShopId shopId) {
        Objects.requireNonNull(shopId, "shopId");
        return find(shopId).orElseGet(() -> save(ShopMenuSnapshot.empty(shopId)));
    }
}

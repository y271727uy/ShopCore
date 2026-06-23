package com.y271727uy.shopcore.core.shop.storage;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;

import java.util.Objects;

public record ShopRuntimeSnapshot(
        ShopInstance shop,
        ShopMenuSnapshot menuSnapshot,
        ShopOrderBook orderBook,
        boolean openRequested
) {
    public ShopRuntimeSnapshot {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(menuSnapshot, "menuSnapshot");
        Objects.requireNonNull(orderBook, "orderBook");
        if (!shop.shopId().equals(menuSnapshot.shopId())) {
            throw new IllegalArgumentException("menu snapshot belongs to another shop");
        }
        if (!shop.shopId().equals(orderBook.shopId())) {
            throw new IllegalArgumentException("order book belongs to another shop");
        }
    }
}

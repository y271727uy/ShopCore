package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;

import java.util.List;
import java.util.Objects;

public record OrderBookLifecycleResult(ShopInstance shop, ShopOrderBook orderBook, List<OrderLifecycleResult> events) {
    public OrderBookLifecycleResult {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(orderBook, "orderBook");
        events = List.copyOf(events);
    }
}

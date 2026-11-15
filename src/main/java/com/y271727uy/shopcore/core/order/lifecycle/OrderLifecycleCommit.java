package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Publishes live holder changes once, after state and settlement have been applied.
 * This is a notification boundary, not a rollback transaction for inventory or bank operations.
 */
public final class OrderLifecycleCommit implements AutoCloseable {
    private final ShopBlockRuntimeHolder holder;
    private final ShopInstance previousShop;
    private final ShopOrderBook previousBook;
    private final Runnable changed;
    private boolean closed;

    private OrderLifecycleCommit(ShopBlockRuntimeHolder holder, Runnable changed) {
        this.holder = Objects.requireNonNull(holder, "holder");
        this.changed = Objects.requireNonNull(changed, "changed");
        previousShop = holder.shopcore$shopInstance();
        previousBook = holder.shopcore$orderBook();
    }

    public static OrderLifecycleCommit begin(ShopBlockRuntimeHolder holder) {
        return begin(holder, holder::shopcore$runtimeChanged);
    }

    public static OrderLifecycleCommit begin(ShopBlockRuntimeHolder holder, Runnable changed) {
        return new OrderLifecycleCommit(holder, changed);
    }

    public static void apply(ShopBlockRuntimeHolder holder, ShopInstance shop, ShopOrderBook book) {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(book, "book");
        try (var commit = begin(holder)) {
            holder.shopcore$setShopInstance(shop);
            holder.shopcore$setOrderBook(book);
        }
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        ShopOrderBook book = holder.shopcore$orderBook();
        if (Objects.equals(previousShop, holder.shopcore$shopInstance()) && Objects.equals(previousBook, book)) {
            return;
        }
        var changes = OrderStateChange.between(statuses(previousBook), statuses(book));
        // Save/sync is requested before optional observers can fail.
        changed.run();
        if (!changes.isEmpty()) {
            holder.shopcore$ordersChanged(changes);
        }
    }

    private static Map<UUID, OrderStatus> statuses(ShopOrderBook book) {
        Map<UUID, OrderStatus> states = new LinkedHashMap<>();
        if (book != null) book.orders().forEach(order -> states.put(order.orderId(), order.status()));
        return states;
    }
}

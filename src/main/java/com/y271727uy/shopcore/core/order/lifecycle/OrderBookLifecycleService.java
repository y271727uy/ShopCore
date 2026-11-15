package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Shared immutable order/status accounting for both runtime drivers and manual cancellation. */
public final class OrderBookLifecycleService {
    private OrderBookLifecycleService() { }

    public static OrderBookLifecycleResult refresh(ShopInstance shop, ShopOrderBook orderBook,
            long gameTime, Optional<ShopOrder> incomingOrder) {
        validate(shop, orderBook);
        Objects.requireNonNull(incomingOrder, "incomingOrder");
        if (gameTime < 0) throw new IllegalArgumentException("gameTime cannot be negative");
        List<OrderLifecycleResult> events = new ArrayList<>();
        for (ShopOrder order : orderBook.orders()) {
            OrderLifecycleResult event = OrderLifecycleService.refresh(order, gameTime);
            shop = account(shop, event);
            if (!order.equals(event.afterOrder())) {
                orderBook = orderBook.replace(event.afterOrder());
            }
            if (event.status() != OrderLifecycleStatus.UNCHANGED) {
                events.add(event);
            }
        }
        // Replaying an already inserted order must not increment ordersCreated again.
        if (incomingOrder.isPresent() && orderBook.find(incomingOrder.get().orderId()).isEmpty()) {
            OrderLifecycleResult event = OrderLifecycleService.tryCreate(shop, orderBook.activeOrders(), incomingOrder.get());
            events.add(event);
            if (event.countsAsCreated()) {
                orderBook = orderBook.add(event.afterOrder());
                shop = account(shop, event);
            }
        }
        return new OrderBookLifecycleResult(shop, orderBook, events);
    }

    public static OrderBookLifecycleResult cancelActive(ShopInstance shop, ShopOrderBook orderBook) {
        validate(shop, orderBook);
        List<OrderLifecycleResult> events = new ArrayList<>();
        for (ShopOrder order : orderBook.orders()) {
            OrderLifecycleResult event = OrderLifecycleService.cancel(order);
            if (event.changed()) {
                orderBook = orderBook.replace(event.afterOrder());
                shop = account(shop, event);
                events.add(event);
            }
        }
        return new OrderBookLifecycleResult(shop, orderBook, events);
    }

    private static ShopInstance account(ShopInstance shop, OrderLifecycleResult event) {
        var stats = OrderLifecycleAccounting.record(shop.currentSession(), event.status());
        return stats == shop.currentSession() ? shop : shop.withCurrentSession(stats);
    }

    private static void validate(ShopInstance shop, ShopOrderBook orderBook) {
        if (!Objects.requireNonNull(shop, "shop").shopId().equals(
                Objects.requireNonNull(orderBook, "orderBook").shopId())) {
            throw new IllegalArgumentException("order book belongs to another shop");
        }
    }
}

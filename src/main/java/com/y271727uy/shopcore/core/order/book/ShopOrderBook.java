package com.y271727uy.shopcore.core.order.book;

import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Order container for one shop instance.
 * Persistence and synchronization are handled by the caller.
 */
public record ShopOrderBook(
        ShopId shopId,
        List<ShopOrder> orders
) {
    public ShopOrderBook {
        Objects.requireNonNull(shopId, "shopId");
        orders = List.copyOf(Objects.requireNonNull(orders, "orders"));
        for (ShopOrder order : orders) {
            Objects.requireNonNull(order, "order");
            if (!shopId.equals(order.shopId())) {
                throw new IllegalArgumentException("order belongs to another shop: " + order.orderId());
            }
        }
    }

    public static ShopOrderBook empty(ShopId shopId) {
        return new ShopOrderBook(shopId, List.of());
    }

    public Optional<ShopOrder> find(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId");
        return orders.stream()
                .filter(order -> order.orderId().equals(orderId))
                .findFirst();
    }

    public List<ShopOrder> activeOrders() {
        return filter(ShopOrder::canReceiveDelivery);
    }

    public List<ShopOrder> pendingOrders() {
        return filter(order -> order.status() == OrderStatus.PENDING);
    }

    public List<ShopOrder> partialOrders() {
        return filter(order -> order.status() == OrderStatus.PARTIAL);
    }

    public List<ShopOrder> completedOrders() {
        return filter(order -> order.status() == OrderStatus.COMPLETED);
    }

    public List<ShopOrder> expiredOrders() {
        return filter(order -> order.status() == OrderStatus.EXPIRED);
    }

    public List<ShopOrder> cancelledOrders() {
        return filter(order -> order.status() == OrderStatus.CANCELLED);
    }

    public int activeOrderCount() {
        int count = 0;
        for (ShopOrder order : orders) {
            if (order.canReceiveDelivery()) {
                count++;
            }
        }
        return count;
    }

    public ShopOrderBook add(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        if (!shopId.equals(order.shopId())) {
            throw new IllegalArgumentException("order belongs to another shop: " + order.orderId());
        }
        if (find(order.orderId()).isPresent()) {
            return replace(order);
        }

        List<ShopOrder> updated = new ArrayList<>(orders);
        updated.add(order);
        return new ShopOrderBook(shopId, sorted(updated));
    }

    public ShopOrderBook replace(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        if (!shopId.equals(order.shopId())) {
            throw new IllegalArgumentException("order belongs to another shop: " + order.orderId());
        }

        boolean replaced = false;
        List<ShopOrder> updated = new ArrayList<>(orders.size() + 1);
        for (ShopOrder current : orders) {
            if (current.orderId().equals(order.orderId())) {
                updated.add(order);
                replaced = true;
            } else {
                updated.add(current);
            }
        }
        if (!replaced) {
            updated.add(order);
        }
        return new ShopOrderBook(shopId, sorted(updated));
    }

    public ShopOrderBook remove(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId");
        List<ShopOrder> updated = orders.stream()
                .filter(order -> !order.orderId().equals(orderId))
                .toList();
        if (updated.size() == orders.size()) {
            return this;
        }
        return new ShopOrderBook(shopId, updated);
    }

    public ShopOrderBook pruneClosed(int maxClosedOrders) {
        if (maxClosedOrders < 0) {
            throw new IllegalArgumentException("maxClosedOrders cannot be negative");
        }

        List<ShopOrder> active = new ArrayList<>();
        List<ShopOrder> closed = new ArrayList<>();
        for (ShopOrder order : orders) {
            if (order.canReceiveDelivery()) {
                active.add(order);
            } else {
                closed.add(order);
            }
        }

        if (closed.size() <= maxClosedOrders) {
            return this;
        }

        closed.sort(Comparator.comparingLong(ShopOrder::createdGameTime).reversed());
        List<ShopOrder> updated = new ArrayList<>(active);
        updated.addAll(closed.subList(0, maxClosedOrders));
        return new ShopOrderBook(shopId, sorted(updated));
    }

    private List<ShopOrder> filter(Predicate<ShopOrder> predicate) {
        return orders.stream()
                .filter(predicate)
                .toList();
    }

    private static List<ShopOrder> sorted(List<ShopOrder> orders) {
        return orders.stream()
                .sorted(Comparator.comparingLong(ShopOrder::createdGameTime).thenComparing(ShopOrder::orderId))
                .toList();
    }
}

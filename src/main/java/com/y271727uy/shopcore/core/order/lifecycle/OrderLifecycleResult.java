package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.order.ShopOrder;

import java.util.Objects;
import java.util.Optional;

public record OrderLifecycleResult(
        OrderLifecycleStatus status,
        ShopOrder beforeOrder,
        ShopOrder afterOrder,
        boolean countsAsCreated,
        boolean countsAsCompleted,
        boolean countsAsExpired,
        boolean countsAsCancelled
) {
    public OrderLifecycleResult {
        Objects.requireNonNull(status, "status");
    }

    public static OrderLifecycleResult created(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        return new OrderLifecycleResult(OrderLifecycleStatus.CREATED, null, order, true, false, false, false);
    }

    public static OrderLifecycleResult rejected(OrderLifecycleStatus status) {
        if (status != OrderLifecycleStatus.CREATE_REJECTED_ORDER_LIMIT
                && status != OrderLifecycleStatus.CREATE_REJECTED_CLOSED) {
            throw new IllegalArgumentException("status is not a create rejection: " + status);
        }
        return new OrderLifecycleResult(status, null, null, false, false, false, false);
    }

    public static OrderLifecycleResult unchanged(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        return new OrderLifecycleResult(OrderLifecycleStatus.UNCHANGED, order, order, false, false, false, false);
    }

    public static OrderLifecycleResult transitioned(OrderLifecycleStatus status, ShopOrder beforeOrder, ShopOrder afterOrder) {
        Objects.requireNonNull(beforeOrder, "beforeOrder");
        Objects.requireNonNull(afterOrder, "afterOrder");
        return new OrderLifecycleResult(
                status,
                beforeOrder,
                afterOrder,
                false,
                status == OrderLifecycleStatus.COMPLETED,
                status == OrderLifecycleStatus.EXPIRED,
                status == OrderLifecycleStatus.CANCELLED
        );
    }

    public static OrderLifecycleResult notCancellable(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        return new OrderLifecycleResult(OrderLifecycleStatus.NOT_CANCELLABLE, order, order, false, false, false, false);
    }

    public Optional<ShopOrder> beforeOrderOptional() {
        return Optional.ofNullable(beforeOrder);
    }

    public Optional<ShopOrder> afterOrderOptional() {
        return Optional.ofNullable(afterOrder);
    }

    public boolean changed() {
        return beforeOrder != afterOrder;
    }
}

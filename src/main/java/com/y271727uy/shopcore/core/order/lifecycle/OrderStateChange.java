package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.order.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Committed status notification. A null previous status means a newly created order. */
public record OrderStateChange(UUID orderId, OrderStatus before, OrderStatus after) {
    public OrderStateChange {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(after, "after");
    }

    public boolean endsOrder() {
        return (before == OrderStatus.PENDING || before == OrderStatus.PARTIAL)
                && (after == OrderStatus.COMPLETED || after == OrderStatus.CANCELLED || after == OrderStatus.EXPIRED);
    }

    public static List<OrderStateChange> between(Map<UUID, OrderStatus> before, Map<UUID, OrderStatus> after) {
        List<OrderStateChange> changes = new ArrayList<>();
        after.forEach((id, status) -> {
            OrderStatus previous = before.get(id);
            if (previous != status) {
                changes.add(new OrderStateChange(id, previous, status));
            }
        });
        return List.copyOf(changes);
    }
}

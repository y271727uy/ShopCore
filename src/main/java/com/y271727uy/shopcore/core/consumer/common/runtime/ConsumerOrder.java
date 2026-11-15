package com.y271727uy.shopcore.core.consumer.common.runtime;

import com.y271727uy.shopcore.core.order.book.ShopOrderBook;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Only the active-order identity and arrival ordering needed by consumer management. */
public record ConsumerOrder(UUID orderId, long createdGameTime) {
    public ConsumerOrder {
        Objects.requireNonNull(orderId, "orderId");
        if (createdGameTime < 0L) {
            throw new IllegalArgumentException("createdGameTime cannot be negative");
        }
    }

    public static List<ConsumerOrder> activeOrders(ShopOrderBook book) {
        Objects.requireNonNull(book, "book");
        return book.activeOrders().stream()
                .map(order -> new ConsumerOrder(order.orderId(), order.createdGameTime()))
                .toList();
    }
}

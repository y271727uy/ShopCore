package com.y271727uy.shopcore.core.order.prompt;

import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.ShopOrder;

import java.util.Objects;

public final class OrderPromptFactory {
    private OrderPromptFactory() {
    }

    public static OrderPrompt fromOrder(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        return new OrderPrompt(
                order.orderId(),
                order.lines().stream()
                        .map(OrderPromptFactory::fromLine)
                        .toList()
        );
    }

    private static OrderPromptLine fromLine(OrderLine line) {
        return new OrderPromptLine(
                line.requestedItem(),
                line.requestedCount(),
                line.requestedItem().getHoverName().getString(),
                line.isComplete()
        );
    }
}

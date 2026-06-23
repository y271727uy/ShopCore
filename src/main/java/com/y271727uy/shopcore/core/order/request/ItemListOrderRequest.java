package com.y271727uy.shopcore.core.order.request;

import com.y271727uy.shopcore.core.order.OrderLine;

import java.util.List;
import java.util.Objects;

/**
 * A checklist order: customer asks for A + B + C as separate deliverable items.
 */
public record ItemListOrderRequest(
        List<OrderLine> lines
) implements OrderRequest {
    public ItemListOrderRequest {
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines cannot be empty");
        }
    }

    public static ItemListOrderRequest single(SingleItemOrderRequest request) {
        Objects.requireNonNull(request, "request");
        return new ItemListOrderRequest(List.of(request.toOrderLine()));
    }

    @Override
    public OrderRequestKind kind() {
        return OrderRequestKind.ITEM_LIST;
    }

    @Override
    public int requestedCount() {
        return lines.stream().mapToInt(OrderLine::requestedCount).sum();
    }

    @Override
    public long baseValue() {
        return lines.stream().mapToLong(OrderLine::totalValue).sum();
    }
}

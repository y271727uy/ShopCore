package com.y271727uy.shopcore.core.order.request;

import com.y271727uy.shopcore.core.order.OrderLine;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * One direct item request: customer asks for A and delivery expects A.
 */
public record SingleItemOrderRequest(
        ItemStack requestedItem,
        int requestedCount,
        int unitPrice
) implements OrderRequest {
    public SingleItemOrderRequest {
        Objects.requireNonNull(requestedItem, "requestedItem");
        if (requestedItem.isEmpty()) {
            throw new IllegalArgumentException("requestedItem cannot be empty");
        }
        requestedItem = requestedItem.copyWithCount(1);
        if (requestedCount < 1) {
            throw new IllegalArgumentException("requestedCount must be at least 1");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice cannot be negative");
        }
    }

    @Override
    public OrderRequestKind kind() {
        return OrderRequestKind.SINGLE_ITEM;
    }

    @Override
    public long baseValue() {
        return (long) requestedCount * unitPrice;
    }

    public OrderLine toOrderLine() {
        return new OrderLine(requestedItem, requestedCount, 0, unitPrice);
    }
}

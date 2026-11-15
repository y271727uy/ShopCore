package com.y271727uy.shopcore.api.component.order;

import net.minecraft.world.item.ItemStack;
import java.util.Objects;

/** Detached line data; returned item stacks can be edited without changing the order. */
public record OrderLineSnapshot(ItemStack requestedItem, int requestedCount, int deliveredCount, int unitPrice) {
    public OrderLineSnapshot {
        requestedItem = Objects.requireNonNull(requestedItem, "requestedItem").copyWithCount(1);
        if (requestedCount < 1 || deliveredCount < 0 || deliveredCount > requestedCount || unitPrice < 0)
            throw new IllegalArgumentException("invalid order line counts or price");
    }
    @Override public ItemStack requestedItem() { return requestedItem.copy(); }
    public int remainingCount() { return requestedCount - deliveredCount; }
    public long totalValue() { return (long) requestedCount * unitPrice; }
}

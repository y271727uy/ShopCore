package com.y271727uy.shopcore.core.order;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record OrderLine(
        ItemStack requestedItem,
        int requestedCount,
        int deliveredCount,
        int unitPrice
) {
    public OrderLine {
        Objects.requireNonNull(requestedItem, "requestedItem");
        if (requestedItem.isEmpty()) {
            throw new IllegalArgumentException("requestedItem cannot be empty");
        }
        requestedItem = requestedItem.copyWithCount(1);
        if (requestedCount < 1) {
            throw new IllegalArgumentException("requestedCount must be at least 1");
        }
        if (deliveredCount < 0 || deliveredCount > requestedCount) {
            throw new IllegalArgumentException("deliveredCount must be in [0, requestedCount]");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice cannot be negative");
        }
    }

    public int remainingCount() {
        return requestedCount - deliveredCount;
    }

    public boolean isComplete() {
        return deliveredCount >= requestedCount;
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, requestedItem);
    }

    public long totalValue() {
        return (long) requestedCount * unitPrice;
    }

    public long deliveredValue() {
        return (long) deliveredCount * unitPrice;
    }

    public OrderLine deliver(int amount) {
        if (amount <= 0) {
            return this;
        }
        return new OrderLine(requestedItem, requestedCount, Math.min(requestedCount, deliveredCount + amount), unitPrice);
    }
}

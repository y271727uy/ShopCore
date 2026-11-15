package com.y271727uy.shopcore.core.order.interaction;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** Delivery evidence for observers; the original hand stack may already be empty. */
public record DeliveryReceipt(ItemStack inputBefore, OrderDeliveryInteractionResult result) {
    public DeliveryReceipt {
        inputBefore = Objects.requireNonNull(inputBefore, "inputBefore").copy();
        Objects.requireNonNull(result, "result");
    }

    @Override
    public ItemStack inputBefore() {
        return inputBefore.copy();
    }

    public int deliveredCount() {
        return result.deliveryResult() == null ? 0 : result.deliveryResult().consumedCount();
    }

    /** Delivery succeeded, independent of whether a completed order's bank deposit succeeded. */
    public boolean delivered() {
        return result.status() == OrderInteractionStatus.DELIVERED
                || result.status() == OrderInteractionStatus.COMPLETED;
    }
}

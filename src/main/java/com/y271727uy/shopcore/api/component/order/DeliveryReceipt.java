package com.y271727uy.shopcore.api.component.order;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** Successful delivery notification, emitted after order state and persistence are committed. */
public record DeliveryReceipt(ItemStack inputBefore, int deliveredCount) {
    public DeliveryReceipt {
        inputBefore = Objects.requireNonNull(inputBefore, "inputBefore").copy();
        if (deliveredCount < 1) throw new IllegalArgumentException("deliveredCount must be positive");
    }

    @Override
    public ItemStack inputBefore() {
        return inputBefore.copy();
    }
}

package com.y271727uy.shopcore.core.order.evaluator.structured;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record StructuredDeliveryPart(
        ResourceLocation partKey,
        ItemStack item,
        int count,
        int position
) {
    public static final int ANY_POSITION = -1;

    public StructuredDeliveryPart {
        Objects.requireNonNull(partKey, "partKey");
        Objects.requireNonNull(item, "item");
        if (item.isEmpty()) {
            throw new IllegalArgumentException("item cannot be empty");
        }
        item = item.copyWithCount(1);
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }
        if (position < ANY_POSITION) {
            throw new IllegalArgumentException("position cannot be less than ANY_POSITION");
        }
    }
}

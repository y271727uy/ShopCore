package com.y271727uy.shopcore.core.order.request;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * One component requirement inside a structured item order.
 * Position is optional for evaluators; use -1 when order does not matter.
 */
public record StructuredOrderPart(
        ResourceLocation partKey,
        ItemStack displayItem,
        int requestedCount,
        int position,
        boolean required
) {
    public static final int ANY_POSITION = -1;

    public StructuredOrderPart {
        Objects.requireNonNull(partKey, "partKey");
        Objects.requireNonNull(displayItem, "displayItem");
        if (displayItem.isEmpty()) {
            throw new IllegalArgumentException("displayItem cannot be empty");
        }
        displayItem = displayItem.copyWithCount(1);
        if (requestedCount < 1) {
            throw new IllegalArgumentException("requestedCount must be at least 1");
        }
        if (position < ANY_POSITION) {
            throw new IllegalArgumentException("position cannot be less than ANY_POSITION");
        }
    }

    public static StructuredOrderPart required(ResourceLocation partKey, ItemStack displayItem, int requestedCount, int position) {
        return new StructuredOrderPart(partKey, displayItem, requestedCount, position, true);
    }

    public static StructuredOrderPart optional(ResourceLocation partKey, ItemStack displayItem, int requestedCount, int position) {
        return new StructuredOrderPart(partKey, displayItem, requestedCount, position, false);
    }
}

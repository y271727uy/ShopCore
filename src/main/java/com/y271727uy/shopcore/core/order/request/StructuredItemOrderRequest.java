package com.y271727uy.shopcore.core.order.request;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * A composed-item request: customer asks for a structure made from parts.
 * Delivery must be evaluated by a structure-aware evaluator instead of exact item equality.
 */
public record StructuredItemOrderRequest(
        ResourceLocation structureType,
        ItemStack displayItem,
        List<StructuredOrderPart> parts,
        int basePrice
) implements OrderRequest {
    public StructuredItemOrderRequest {
        Objects.requireNonNull(structureType, "structureType");
        Objects.requireNonNull(displayItem, "displayItem");
        if (displayItem.isEmpty()) {
            throw new IllegalArgumentException("displayItem cannot be empty");
        }
        displayItem = displayItem.copyWithCount(1);
        parts = List.copyOf(Objects.requireNonNull(parts, "parts"));
        if (parts.isEmpty()) {
            throw new IllegalArgumentException("parts cannot be empty");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException("basePrice cannot be negative");
        }
    }

    @Override
    public OrderRequestKind kind() {
        return OrderRequestKind.STRUCTURED_ITEM;
    }

    @Override
    public int requestedCount() {
        return parts.stream().mapToInt(StructuredOrderPart::requestedCount).sum();
    }

    @Override
    public long baseValue() {
        return basePrice;
    }
}

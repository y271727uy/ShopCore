package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record OrderCandidate(
        ShopListing listing,
        ItemStack item,
        ResourceLocation itemId,
        ResourceLocation demandCategory,
        OrderComplexity complexity,
        int quantity,
        int unitPrice
) {
    public OrderCandidate {
        Objects.requireNonNull(listing, "listing");
        Objects.requireNonNull(item, "item");
        if (item.isEmpty()) {
            throw new IllegalArgumentException("item cannot be empty");
        }
        item = item.copyWithCount(1);
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(demandCategory, "demandCategory");
        Objects.requireNonNull(complexity, "complexity");
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("unitPrice cannot be negative");
        }
    }

    public long totalValue() {
        return (long) quantity * unitPrice;
    }

    public OrderLine toOrderLine() {
        return new OrderLine(item, quantity, 0, unitPrice);
    }
}

package com.y271727uy.shopcore.core.order.customer.special;

import com.y271727uy.shopcore.core.order.OrderLine;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * Custom order override for special customers
 *
 * Can specify exact items or allow generation with constraints
 */
public record OrderOverride(
    List<ItemStack> requestedItems,
    OverrideMode mode
) {
    public OrderOverride {
        requestedItems = List.copyOf(Objects.requireNonNull(requestedItems, "requestedItems"));
        Objects.requireNonNull(mode, "mode");
    }

    /**
     * Create fixed order with exact items
     */
    public static OrderOverride fixed(List<ItemStack> items) {
        return new OrderOverride(items, OverrideMode.FIXED);
    }

    /**
     * Create fixed order with single item
     */
    public static OrderOverride fixed(ItemStack item) {
        return new OrderOverride(List.of(item), OverrideMode.FIXED);
    }

    /**
     * Allow generation but prefer these items
     */
    public static OrderOverride preferred(List<ItemStack> items) {
        return new OrderOverride(items, OverrideMode.PREFERRED);
    }

    public enum OverrideMode {
        /**
         * Use exactly these items, no generation
         */
        FIXED,

        /**
         * Prefer these items but can add more from menu
         */
        PREFERRED
    }
}

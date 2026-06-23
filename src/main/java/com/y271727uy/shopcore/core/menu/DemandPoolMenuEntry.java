package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.menu.pool.DemandPoolKey;

import java.util.Objects;

public record DemandPoolMenuEntry(
        int slotIndex,
        String menuId,
        boolean enabled,
        DemandPoolKey poolKey,
        int maxSelections
) implements ShopMenuEntry {
    public DemandPoolMenuEntry {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex cannot be negative");
        }
        menuId = ShopMenuEntry.normalizeMenuId(menuId);
        Objects.requireNonNull(poolKey, "poolKey");
        if (maxSelections < 1) {
            throw new IllegalArgumentException("maxSelections must be at least 1");
        }
    }

    @Override
    public ShopMenuEntryKind kind() {
        return ShopMenuEntryKind.DEMAND_POOL;
    }
}

package com.y271727uy.shopcore.core.menu;

import java.util.Objects;

public sealed interface ShopMenuEntry permits ListingMenuEntry, DemandPoolMenuEntry {
    int slotIndex();

    String menuId();

    boolean enabled();

    ShopMenuEntryKind kind();

    static String normalizeMenuId(String menuId) {
        String normalized = Objects.requireNonNull(menuId, "menuId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("menuId cannot be blank");
        }
        return normalized;
    }
}

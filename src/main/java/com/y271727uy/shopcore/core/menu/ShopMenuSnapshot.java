package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record ShopMenuSnapshot(
        ShopId shopId,
        List<ShopMenuEntry> entries,
        long version
) {
    public ShopMenuSnapshot {
        Objects.requireNonNull(shopId, "shopId");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries").stream()
                .sorted(Comparator.comparingInt(ShopMenuEntry::slotIndex))
                .toList());
        if (version < 0L) {
            throw new IllegalArgumentException("version cannot be negative");
        }
    }

    public static ShopMenuSnapshot empty(ShopId shopId) {
        return new ShopMenuSnapshot(shopId, List.of(), 0L);
    }

    public List<ShopMenuEntry> enabledEntries() {
        return entries.stream()
                .filter(ShopMenuEntry::enabled)
                .toList();
    }

    public ShopMenuSnapshot withEntries(List<ShopMenuEntry> entries) {
        return new ShopMenuSnapshot(shopId, entries, version + 1L);
    }

    public boolean isEmpty() {
        return enabledEntries().isEmpty();
    }
}

package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.shop.instance.ShopId;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

    /**
     * Check if the menu has any listing with the given menu ID
     */
    public boolean hasMenuId(String menuId) {
        return entries.stream()
            .filter(ShopMenuEntry::enabled)
            .anyMatch(entry -> entry.menuId().equals(menuId));
    }

    /**
     * Check if the menu has any listing for the given item
     * (requires resolving menu Isto items via tag system - not implemented here)
     */
    public boolean hasListing(Item item) {
        // TODO: This requires MenuResolver to resolve menuId -> ItemStack
        // For now, special customers should use hasMenuId() or check directly
        throw new UnsupportedOperationException("Use hasMenuId() or provide MenuResolver");
    }
}

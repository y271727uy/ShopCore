package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.order.ShopListing;

import java.util.Objects;

public record ListingMenuEntry(
        int slotIndex,
        String menuId,
        boolean enabled,
        ShopListing listing
) implements ShopMenuEntry {
    public ListingMenuEntry {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex cannot be negative");
        }
        menuId = ShopMenuEntry.normalizeMenuId(menuId);
        Objects.requireNonNull(listing, "listing");
    }

    public static ListingMenuEntry of(ShopListing listing) {
        Objects.requireNonNull(listing, "listing");
        return new ListingMenuEntry(listing.slotIndex(), listing.menuId(), listing.enabled(), listing);
    }

    @Override
    public ShopMenuEntryKind kind() {
        return ShopMenuEntryKind.LISTING;
    }
}

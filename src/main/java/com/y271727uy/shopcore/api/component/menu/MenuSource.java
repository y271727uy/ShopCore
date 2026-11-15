package com.y271727uy.shopcore.api.component.menu;

import java.util.List;

/** Supplies offers when a shop has no enabled menu entries, including the existing empty-save fallback. */
@FunctionalInterface
public interface MenuSource {
    List<MenuListing> listings();

    static MenuSource fixed(MenuListing... listings) {
        List<MenuListing> copy = List.of(listings);
        return () -> copy;
    }
}

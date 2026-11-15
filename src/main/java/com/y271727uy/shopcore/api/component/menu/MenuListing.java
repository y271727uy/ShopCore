package com.y271727uy.shopcore.api.component.menu;

import java.util.Objects;

/** A manually priced, single-item offer. The reference can be an item id or a configured menu id. */
public record MenuListing(String reference, int unitPrice, int maxPerOrder) {
    public MenuListing {
        reference = Objects.requireNonNull(reference, "reference").trim();
        if (reference.isEmpty()) throw new IllegalArgumentException("reference cannot be blank");
        if (unitPrice < 0) throw new IllegalArgumentException("unitPrice cannot be negative");
        if (maxPerOrder < 1) throw new IllegalArgumentException("maxPerOrder must be positive");
    }
}

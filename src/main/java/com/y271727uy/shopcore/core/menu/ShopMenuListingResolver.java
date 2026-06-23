package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolSelectionContext;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolSelectionResult;
import com.y271727uy.shopcore.core.order.ShopListing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopMenuListingResolver {
    private final DemandPoolCatalog demandPoolCatalog;

    public ShopMenuListingResolver(DemandPoolCatalog demandPoolCatalog) {
        this.demandPoolCatalog = Objects.requireNonNull(demandPoolCatalog, "demandPoolCatalog");
    }

    public List<ShopListing> resolve(ShopMenuSnapshot snapshot, DemandPoolSelectionContext context) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(context, "context");

        List<ShopListing> listings = new ArrayList<>();
        for (ShopMenuEntry entry : snapshot.enabledEntries()) {
            if (entry instanceof ListingMenuEntry listingEntry) {
                listings.add(listingEntry.listing());
            } else if (entry instanceof DemandPoolMenuEntry demandPoolEntry) {
                DemandPoolSelectionResult result = demandPoolCatalog.select(demandPoolEntry, context);
                listings.addAll(result.listings());
            }
        }
        return List.copyOf(listings);
    }
}

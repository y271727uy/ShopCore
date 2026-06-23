package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.menu.DemandPoolMenuEntry;
import com.y271727uy.shopcore.core.order.ShopListing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RandomDemandPoolSelector implements DemandPoolSelector {
    public static final RandomDemandPoolSelector INSTANCE = new RandomDemandPoolSelector();

    @Override
    public DemandPoolSelectionResult select(DemandPool pool, DemandPoolMenuEntry entry, DemandPoolSelectionContext context) {
        Objects.requireNonNull(pool, "pool");
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(context, "context");

        List<ShopListing> enabled = pool.listings().stream()
                .filter(ShopListing::enabled)
                .toList();
        if (enabled.isEmpty()) {
            return DemandPoolSelectionResult.empty(pool.key());
        }

        int selectionCount = Math.min(entry.maxSelections(), enabled.size());
        List<ShopListing> remaining = new ArrayList<>(enabled);
        List<ShopListing> selected = new ArrayList<>(selectionCount);
        for (int i = 0; i < selectionCount; i++) {
            selected.add(remaining.remove(context.random().nextInt(remaining.size())));
        }
        return new DemandPoolSelectionResult(pool.key(), selected);
    }
}

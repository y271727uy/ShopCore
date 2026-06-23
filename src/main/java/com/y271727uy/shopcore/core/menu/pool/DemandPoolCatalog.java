package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.menu.DemandPoolMenuEntry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DemandPoolCatalog {
    private final Map<DemandPoolKey, DemandPool> pools = new LinkedHashMap<>();
    private final DemandPoolSelector selector;

    public DemandPoolCatalog() {
        this(RandomDemandPoolSelector.INSTANCE);
    }

    public DemandPoolCatalog(DemandPoolSelector selector) {
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    public void clear() {
        pools.clear();
    }

    public DemandPool register(DemandPool pool) {
        Objects.requireNonNull(pool, "pool");
        pools.put(pool.key(), pool);
        return pool;
    }

    public Optional<DemandPool> find(DemandPoolKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(pools.get(key));
    }

    public DemandPoolSelectionResult select(DemandPoolMenuEntry entry, DemandPoolSelectionContext context) {
        Objects.requireNonNull(entry, "entry");
        Objects.requireNonNull(context, "context");
        return find(entry.poolKey())
                .map(pool -> selector.select(pool, entry, context))
                .orElseGet(() -> DemandPoolSelectionResult.empty(entry.poolKey()));
    }

    public Map<DemandPoolKey, DemandPool> snapshot() {
        return Map.copyOf(pools);
    }
}

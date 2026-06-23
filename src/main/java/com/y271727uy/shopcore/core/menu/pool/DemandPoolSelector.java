package com.y271727uy.shopcore.core.menu.pool;

import com.y271727uy.shopcore.core.menu.DemandPoolMenuEntry;

public interface DemandPoolSelector {
    DemandPoolSelectionResult select(DemandPool pool, DemandPoolMenuEntry entry, DemandPoolSelectionContext context);
}

package com.y271727uy.shopcore.integration.jei.sdmshop.event;

import net.minecraftforge.eventbus.api.Event;

import java.util.Objects;

/**
 * Fired when SDM finishes building a shop entry and JEI should absorb it.
 */
public final class ShopDataLoadedEvent extends Event {
    private final Object shopTab;
    private final Object entry;

    public ShopDataLoadedEvent(Object shopTab, Object entry) {
        this.shopTab = Objects.requireNonNull(shopTab, "shopTab");
        this.entry = Objects.requireNonNull(entry, "entry");
    }

    public Object getShopTab() {
        return shopTab;
    }

    public Object getEntry() {
        return entry;
    }
}


package com.y271727uy.shopcore.api;

import java.util.Set;

/**
 * Version and capability metadata for the component-based ShopCore API.
 */
public final class ShopcoreApi {
    public static final String API_VERSION = "3.0.0";
    public static final Set<String> CAPABILITIES = Set.of(
            "economic.currency",
            "economic.prices",
            "economic.checkout",
            "menu.catalog",
            "shop.infrastructure",
            "shop.settlement_account",
            "shop.preset.horizontal_queue",
            "shop.components.menu",
            "shop.components.orders",
            "shop.orders.snapshots",
            "shop.delivery.results",
            "shop.components.queue_consumers"
    );

    private ShopcoreApi() {
    }

    public static String version() {
        return API_VERSION;
    }

    public static boolean supports(String capability) {
        return capability != null && CAPABILITIES.contains(capability);
    }
}

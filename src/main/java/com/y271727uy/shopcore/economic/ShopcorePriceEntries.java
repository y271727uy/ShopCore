package com.y271727uy.shopcore.economic;


public final class ShopcorePriceEntries {
    private ShopcorePriceEntries() {
    }

    public static void registerAll() {
        PriceRegistry.registerEntry("minecraft:apple", 10, 2, 0);
        PriceRegistry.registerEntry("#minecraft:logs", 12, 3, 1);
        PriceRegistry.registerEntry("minecraft:diamond", 180, 40, 10);
        PriceRegistry.registerEntry("minecraft:gold_ingot", 64, 12, 3);
    }
}


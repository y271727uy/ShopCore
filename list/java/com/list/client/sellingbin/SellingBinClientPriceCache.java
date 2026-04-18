package com.list.client.sellingbin;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class SellingBinClientPriceCache {
    private static Map<ResourceLocation, Integer> priceBonusByRecipe = Map.of();

    private SellingBinClientPriceCache() {
    }

    public static void applySnapshot(Map<ResourceLocation, Integer> updatedSnapshot) {
        priceBonusByRecipe = Map.copyOf(new HashMap<>(updatedSnapshot));
    }

    public static int getPriceBonus(ResourceLocation recipeId) {
        return priceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    public static void clear() {
        priceBonusByRecipe = Map.of();
    }
}

package com.y271727uy.shopcore.client.sellingbin;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class SellingBinClientPriceCache {
    private static Map<ResourceLocation, Integer> totalPriceBonusByRecipe = Map.of();
    private static Map<ResourceLocation, Integer> seasonalPriceBonusByRecipe = Map.of();

    private SellingBinClientPriceCache() {
    }

    public static void applySnapshot(Map<ResourceLocation, Integer> updatedTotalSnapshot, Map<ResourceLocation, Integer> updatedSeasonalSnapshot) {
        totalPriceBonusByRecipe = Map.copyOf(new HashMap<>(updatedTotalSnapshot));
        seasonalPriceBonusByRecipe = Map.copyOf(new HashMap<>(updatedSeasonalSnapshot));
    }

    public static int getPriceBonus(ResourceLocation recipeId) {
        return totalPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    public static int getSeasonalPriceBonus(ResourceLocation recipeId) {
        return seasonalPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    public static void clear() {
        totalPriceBonusByRecipe = Map.of();
        seasonalPriceBonusByRecipe = Map.of();
    }
}

package com.y271727uy.shopcore.client.sellingbin;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public final class SellingBinClientPriceCache {
    private static Map<ResourceLocation, Integer> floatingPriceBonusByRecipe = Map.of();
    private static Map<ResourceLocation, Integer> virtualStockPriceBonusByRecipe = Map.of();
    private static Map<ResourceLocation, Integer> seasonalPriceBonusByRecipe = Map.of();

    private SellingBinClientPriceCache() {
    }

    public static void applySnapshot(
            Map<ResourceLocation, Integer> updatedFloatingSnapshot,
            Map<ResourceLocation, Integer> updatedVirtualStockSnapshot,
            Map<ResourceLocation, Integer> updatedSeasonalSnapshot
    ) {
        floatingPriceBonusByRecipe = Map.copyOf(new HashMap<>(updatedFloatingSnapshot));
        virtualStockPriceBonusByRecipe = Map.copyOf(new HashMap<>(updatedVirtualStockSnapshot));
        seasonalPriceBonusByRecipe = Map.copyOf(new HashMap<>(updatedSeasonalSnapshot));
    }

    public static int getPriceBonus(ResourceLocation recipeId) {
        long total = (long) floatingPriceBonusByRecipe.getOrDefault(recipeId, 0)
                + virtualStockPriceBonusByRecipe.getOrDefault(recipeId, 0)
                + seasonalPriceBonusByRecipe.getOrDefault(recipeId, 0);
        if (total <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (total >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    public static int getFloatingPriceBonus(ResourceLocation recipeId) {
        return floatingPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    public static int getVirtualStockPriceBonus(ResourceLocation recipeId) {
        return virtualStockPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    public static int getSeasonalPriceBonus(ResourceLocation recipeId) {
        return seasonalPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    public static void clear() {
        floatingPriceBonusByRecipe = Map.of();
        virtualStockPriceBonusByRecipe = Map.of();
        seasonalPriceBonusByRecipe = Map.of();
    }
}

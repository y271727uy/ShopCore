package com.y271727uy.shopcore.client.sellingbin;

import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.recipe.SellingBinRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class SellingBinClientPriceHelper {
    private SellingBinClientPriceHelper() {
    }

    public static Optional<SellingBinRecipe> findRecipe(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.getConnection() == null || stack.isEmpty()) {
            return Optional.empty();
        }

        return mc.getConnection().getRecipeManager().getRecipeFor(
                ModRecipes.SELLING_BIN_RECIPE_TYPE.get(),
                new SellingBinRecipe.RecipeInput(List.of(stack)),
                mc.level
        );
    }

    public static int getPriceBonus(SellingBinRecipe recipe) {
        return getPriceBonus(recipe, recipe.getPrimaryInputPreview());
    }

    public static int getPriceBonus(SellingBinRecipe recipe, ItemStack stack) {
        return SellingBinClientPriceCache.getPriceBonus(recipe.getPriceKey(stack));
    }

    public static ItemStack getDisplayOutput(SellingBinRecipe recipe) {
        return getDisplayOutput(recipe, recipe.getPrimaryInputPreview());
    }

    public static ItemStack getDisplayOutput(SellingBinRecipe recipe, ItemStack stack) {
        return recipe.getDisplayOutput(getPriceBonus(recipe, stack));
    }

    public static ItemStack getDisplayInput(SellingBinRecipe recipe, ItemStack stack) {
        ItemStack preview = stack.copy();
        preview.setCount(1);
        return preview;
    }

    public static ItemStack getPreviewOutput(SellingBinRecipe recipe) {
        ItemStack preview = recipe.output.copy();
        preview.setCount(1);
        return preview;
    }

    public static String getPriceText(SellingBinRecipe recipe) {
        return getPriceText(recipe, recipe.getPrimaryInputPreview());
    }

    public static String getPriceText(SellingBinRecipe recipe, ItemStack stack) {
        int priceBonus = getPriceBonus(recipe, stack);
        int min = recipe.getMinOutputCount(priceBonus);
        int max = recipe.getMaxOutputCount(priceBonus);

        if (min == max) {
            return Integer.toString(min);
        }
        return min + "-" + max;
    }

    public static Component getCompactPriceText(SellingBinRecipe recipe) {
        return Component.literal(getPriceText(recipe));
    }
}


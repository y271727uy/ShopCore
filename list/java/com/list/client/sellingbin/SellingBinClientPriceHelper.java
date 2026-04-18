 package com.list.client.sellingbin;

import com.list.all.ModRecipes;
import com.list.recipe.SellingBinRecipe;
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
        return SellingBinClientPriceCache.getPriceBonus(recipe.getId());
    }

    public static ItemStack getDisplayOutput(SellingBinRecipe recipe) {
        return recipe.getDisplayOutput(getPriceBonus(recipe));
    }

    public static ItemStack getPreviewOutput(SellingBinRecipe recipe) {
        ItemStack preview = recipe.output.copy();
        preview.setCount(1);
        return preview;
    }

    public static String getPriceText(SellingBinRecipe recipe) {
        int priceBonus = getPriceBonus(recipe);
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


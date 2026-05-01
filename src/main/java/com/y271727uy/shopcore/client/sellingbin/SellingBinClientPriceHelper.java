package com.y271727uy.shopcore.client.sellingbin;

import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.integration.sereneseasons.SereneSeasonsCompat;
import com.y271727uy.shopcore.recipe.SellingBinRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("unused")
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

    public static int getSeasonalPriceBonus(SellingBinRecipe recipe) {
        return getSeasonalPriceBonus(recipe, recipe.getPrimaryInputPreview());
    }

    public static int getSeasonalPriceBonus(SellingBinRecipe recipe, ItemStack stack) {
        return SellingBinClientPriceCache.getSeasonalPriceBonus(recipe.getPriceKey(stack));
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

    public static String getSeasonalBonusText(SellingBinRecipe recipe, ItemStack stack) {
        int seasonalBonus = getSeasonalPriceBonus(recipe, stack);
        if (seasonalBonus == 0) {
            return "";
        }

        return Component.translatable(getSeasonalBonusTranslationKey()).getString();
    }

    public static String getSeasonalBonusText(SellingBinRecipe recipe) {
        return getSeasonalBonusText(recipe, recipe.getPrimaryInputPreview());
    }

    public static Component getCompactPriceText(SellingBinRecipe recipe) {
        return Component.literal(getPriceText(recipe));
    }

    private static String getSeasonalBonusTranslationKey() {
        Minecraft mc = Minecraft.getInstance();
        String seasonId = SereneSeasonsCompat.getCurrentSeasonId(mc.level).orElse("unknown");
        String normalized = seasonId == null ? "unknown" : seasonId.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "spring" -> "tooltip.shopcore.selling_bin.season_bonus.spring";
            case "summer" -> "tooltip.shopcore.selling_bin.season_bonus.summer";
            case "autumn", "fall" -> "tooltip.shopcore.selling_bin.season_bonus.autumn";
            case "winter" -> "tooltip.shopcore.selling_bin.season_bonus.winter";
            default -> "tooltip.shopcore.selling_bin.season_bonus.unknown";
        };
    }

}


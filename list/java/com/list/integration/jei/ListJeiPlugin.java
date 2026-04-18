package com.list.integration.jei;

import com.list.ListMod;
import com.list.all.ModBlocks;
import com.list.all.ModMenus;
import com.list.all.ModRecipes;
import com.list.integration.jei.category.ForestryGreenhouseCategory;
import com.list.integration.jei.category.ForestryHybridizerCategory;
import com.list.integration.jei.category.FishPondCategory;
import com.list.integration.jei.category.SellingBinCategory;
import com.list.menu.ForestryGreenhouseMenu;
import com.list.menu.ForestryHybridizerMenu;
import com.list.menu.FishPondMenu;
import com.list.menu.SellingBinMenu;
import com.list.recipe.ForestryGreenhouseRecipe;
import com.list.recipe.ForestryHybridizerRecipe;
import com.list.recipe.FishPondRecipe;
import com.list.recipe.SellingBinRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ListJeiPlugin implements IModPlugin {
    public static final RecipeType<FishPondRecipe> FISH_POND = RecipeType.create(ListMod.MODID, "fish_pond", FishPondRecipe.class);
    public static final RecipeType<ForestryHybridizerRecipe> FORESTRY_HYBRIDIZER = RecipeType.create(ListMod.MODID, "forestry_hybridizer", ForestryHybridizerRecipe.class);
    public static final RecipeType<ForestryGreenhouseRecipe> FORESTRY_GREENHOUSE = RecipeType.create(ListMod.MODID, "forestry_greenhouse", ForestryGreenhouseRecipe.class);
    public static final RecipeType<SellingBinRecipe> SELLING_BIN = RecipeType.create(ListMod.MODID, "selling_bin", SellingBinRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ListMod.rl("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        registration.addRecipeCategories(
            new FishPondCategory(jeiHelpers),
            new ForestryHybridizerCategory(jeiHelpers),
            new ForestryGreenhouseCategory(jeiHelpers),
            new SellingBinCategory(jeiHelpers)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        RecipeManager recipeManager = mc.level.getRecipeManager();
        registration.addRecipes(
            FISH_POND,
            recipeManager.getAllRecipesFor(ModRecipes.FISH_POND_RECIPE_TYPE.get())
        );

        registration.addRecipes(
            FORESTRY_HYBRIDIZER,
            recipeManager.getAllRecipesFor(ModRecipes.FORESTRY_HYBRIDIZER_TYPE.get())
        );

        registration.addRecipes(
            FORESTRY_GREENHOUSE,
            recipeManager.getAllRecipesFor(ModRecipes.FORESTRY_GREENHOUSE_TYPE.get())
        );

        registration.addRecipes(
            SELLING_BIN,
            recipeManager.getAllRecipesFor(ModRecipes.SELLING_BIN_RECIPE_TYPE.get())
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModBlocks.FISHPOND_CORE, FISH_POND);
        registration.addRecipeCatalyst(ModBlocks.FORESTRY_HYBRIDIZER, FORESTRY_HYBRIDIZER);
        registration.addRecipeCatalyst(ModBlocks.FORESTRY_GREENHOUSE, FORESTRY_GREENHOUSE);
        registration.addRecipeCatalyst(ModBlocks.SELLING_BIN, SELLING_BIN);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(FishPondMenu.class, ModMenus.FISH_POND.get(), FISH_POND,36, 9,0,36);
        // TE inputs: 3 slots; player inventory: 36 slots
        registration.addRecipeTransferHandler(ForestryHybridizerMenu.class, ModMenus.FORESTRY_HYBRIDIZER.get(), FORESTRY_HYBRIDIZER, 36, 3, 0, 36);
        // TE inputs: 9 basic slots + trench slot (10) = 10; player inventory: 36 slots
        registration.addRecipeTransferHandler(ForestryGreenhouseMenu.class, ModMenus.FORESTRY_GREENHOUSE.get(), FORESTRY_GREENHOUSE, 36, 10, 0, 36);
        // TE inputs: 27 slots; player inventory: 36 slots
        registration.addRecipeTransferHandler(SellingBinMenu.class, ModMenus.SELLING_BIN.get(), SELLING_BIN, 36, 27, 0, 36);
    }
}

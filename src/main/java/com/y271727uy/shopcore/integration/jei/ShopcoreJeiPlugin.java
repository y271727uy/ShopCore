package com.y271727uy.shopcore.integration.jei;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.all.ModBlock;
import com.y271727uy.shopcore.all.ModMenus;
import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.client.menu.SellingBinMenu;
import com.y271727uy.shopcore.integration.jei.category.SellingBinCategory;
import com.y271727uy.shopcore.recipe.SellingBinRecipe;
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
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@JeiPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ShopcoreJeiPlugin implements IModPlugin {
	public static final RecipeType<SellingBinRecipe> SELLING_BIN = RecipeType.create(ShopcoreMod.MODID, "selling_bin", SellingBinRecipe.class);

	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IJeiHelpers jeiHelpers = registration.getJeiHelpers();
		registration.addRecipeCategories(new SellingBinCategory(jeiHelpers));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			return;
		}

		registration.addRecipes(
				SELLING_BIN,
				mc.level.getRecipeManager().getAllRecipesFor(ModRecipes.SELLING_BIN_RECIPE_TYPE.get())
		);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(ModBlock.SELLING_BIN.get()), SELLING_BIN);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(SellingBinMenu.class, ModMenus.SELLING_BIN.get(), SELLING_BIN, 36, 27, 0, 36);
	}
}



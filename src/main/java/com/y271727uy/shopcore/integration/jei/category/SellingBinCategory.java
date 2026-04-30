package com.y271727uy.shopcore.integration.jei.category;

import com.y271727uy.shopcore.all.ModBlock;
import com.y271727uy.shopcore.all.ModItem;
import com.y271727uy.shopcore.client.sellingbin.SellingBinClientPriceHelper;
import com.y271727uy.shopcore.integration.jei.ShopcoreJeiPlugin;
import com.y271727uy.shopcore.recipe.SellingBinRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SellingBinCategory implements IRecipeCategory<SellingBinRecipe> {
	private static final int IN_X = 20;
	private static final int IN_Y = 25;
	private static final int OUT_X = 110;
	private static final int OUT_Y = 25;
	private static final int SEPARATOR_X = 66;
	private static final int SEPARATOR_Y = 25;
	private static final int SLOT_SIZE = 18;
	private static final int OVERLAY_Z_OFFSET = 200;
	private static final int OVERLAY_OFFSET_X = 1;
	private static final int OVERLAY_OFFSET_Y = 1;

	private final mezz.jei.api.recipe.RecipeType<SellingBinRecipe> recipeType = ShopcoreJeiPlugin.SELLING_BIN;
	private final Component title = Component.literal("Selling Bin");
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slot;

	public SellingBinCategory(IJeiHelpers helpers) {
		this.background = helpers.getGuiHelper().createBlankDrawable(150, 60);
		this.icon = helpers.getGuiHelper().createDrawableItemStack(new ItemStack(ModBlock.SELLING_BIN.get()));
		this.slot = helpers.getGuiHelper().getSlotDrawable();
	}

	@Override
	public mezz.jei.api.recipe.RecipeType<SellingBinRecipe> getRecipeType() {
		return recipeType;
	}

	@Override
	public Component getTitle() {
		return title;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SellingBinRecipe recipe, IFocusGroup focuses) {
		builder.addSlot(RecipeIngredientRole.INPUT, IN_X, IN_Y)
				.addIngredients(recipe.input);

		builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X, OUT_Y)
				.addItemStack(SellingBinClientPriceHelper.getDisplayOutput(recipe));
	}

	@Override
	public void draw(SellingBinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		slot.draw(guiGraphics, IN_X - 1, IN_Y - 1);
		slot.draw(guiGraphics, OUT_X - 1, OUT_Y - 1);

		guiGraphics.renderItem(ModItem.EQUALS.get().getDefaultInstance(), SEPARATOR_X, SEPARATOR_Y);
		renderPriceOverlay(guiGraphics, SellingBinClientPriceHelper.getPriceText(recipe));
	}

	private static void renderPriceOverlay(GuiGraphics guiGraphics, String text) {
		if (text.isEmpty()) {
			return;
		}

		var font = Minecraft.getInstance().font;
		int textWidth = Math.max(1, font.width(text));
		float scale = Math.min(1.0F, 16.0F / textWidth);
		float inverseScale = 1.0F / scale;

		int scaledTextWidth = Math.round(textWidth * scale);
		int drawX = OUT_X + SLOT_SIZE - 1 - scaledTextWidth + OVERLAY_OFFSET_X;
		int drawY = OUT_Y + SLOT_SIZE - 1 - Math.round(font.lineHeight * scale) + OVERLAY_OFFSET_Y;

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, OVERLAY_Z_OFFSET);
		guiGraphics.pose().scale(scale, scale, 1.0F);
		guiGraphics.drawString(font, text, Math.round(drawX * inverseScale), Math.round(drawY * inverseScale), 0xFFFFFF, true);
		guiGraphics.pose().popPose();
	}
}






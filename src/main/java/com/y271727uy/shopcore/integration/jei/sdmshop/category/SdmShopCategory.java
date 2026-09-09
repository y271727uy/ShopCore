package com.y271727uy.shopcore.integration.jei.sdmshop.category;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopCurrencyItems;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopJeiEntry;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopUIUtils;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** JEI view of SDM Shop entries collected by {@code SdmShopDataBridge}. */
public final class SdmShopCategory implements IRecipeCategory<SdmShopJeiEntry> {
    public static final RecipeType<SdmShopJeiEntry> RECIPE_TYPE = RecipeType.create(ShopcoreMod.MODID, "sdm_shop", SdmShopJeiEntry.class);
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable recipeArrow;

    public SdmShopCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(112, 38);
        recipeArrow = guiHelper.getRecipeArrow();
        ItemStack currency = SdmShopCurrencyItems.copperCoin();
        icon = guiHelper.createDrawableItemStack(currency.isEmpty() ? new ItemStack(Items.EMERALD) : currency);
    }

    @Override public RecipeType<SdmShopJeiEntry> getRecipeType() { return RECIPE_TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.shopcore.sdm_shop"); }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SdmShopJeiEntry entry, IFocusGroup focuses) {
        ItemStack item = entry.itemStack().copy();
        item.setCount(1);
        ItemStack currency = currencyStack(entry.currency());
        if (entry.isSell()) {
            addItemSlot(builder, RecipeIngredientRole.INPUT, 6, item, entry);
            addCurrencySlot(builder, RecipeIngredientRole.OUTPUT, 88, currency, entry);
        } else {
            addCurrencySlot(builder, RecipeIngredientRole.INPUT, 6, currency, entry);
            addItemSlot(builder, RecipeIngredientRole.OUTPUT, 88, item, entry);
        }
    }

    @Override
    public void draw(SdmShopJeiEntry entry, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        recipeArrow.draw(graphics, 50, 11);
    }

    @Override
    public boolean handleInput(SdmShopJeiEntry entry, double mouseX, double mouseY, InputConstants.Key key) {
        if (!entry.locked() && key.getType() == InputConstants.Type.MOUSE && key.getValue() == 0
                && mouseX >= 0 && mouseX < getWidth() && mouseY >= 0 && mouseY < getHeight()) {
            SdmShopUIUtils.openShopGui(entry);
            return true;
        }
        return false;
    }

    private static void addItemSlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, ItemStack stack, SdmShopJeiEntry entry) {
        builder.addSlot(role, x, 10).addItemStack(stack).addTooltipCallback((view, tooltip) -> {
            tooltip.add(Component.translatable("jei.shopcore.sdm_shop.shop", entry.shopName()));
            if (entry.locked()) {
                tooltip.add(Component.translatable("gui.shopjei.lock_info"));
                if (!entry.lockReason().isBlank()) tooltip.add(Component.literal(entry.lockReason()));
            }
        });
    }

    private static void addCurrencySlot(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, ItemStack stack, SdmShopJeiEntry entry) {
        builder.addSlot(role, x, 10).addItemStack(stack)
                .addTooltipCallback((view, tooltip) -> tooltip.add(Component.translatable("jei.shopcore.sdm_shop.price", entry.priceText())));
    }

    private static ItemStack currencyStack(String currencyId) {
        ItemStack currency = SdmShopCurrencyItems.resolve(currencyId);
        if (currency.isEmpty()) currency = new ItemStack(Items.EMERALD);
        // Stack counts are not a price display. The precise price is shown in the slot tooltip.
        currency.setCount(1);
        return currency;
    }
}



package com.list.integration.jei.category;

import com.list.all.ModBlocks;
import com.list.all.ModItems;
import com.list.client.sellingbin.SellingBinClientPriceHelper;
import com.list.integration.jei.ListJeiPlugin;
import com.list.integration.jei.tooltip.SellingBinTooltipComponent;
import com.list.recipe.SellingBinRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SellingBinCategory extends AbstractRecipeCategory<SellingBinRecipe> {

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

    private final IDrawable slot;

    public SellingBinCategory(IJeiHelpers helpers) {
        super(
            ListJeiPlugin.SELLING_BIN,
            Component.literal("Selling Bin"),
            helpers.getGuiHelper().createDrawableItemStack(ModBlocks.SELLING_BIN.asStack()),
            150,
            60
        );
        this.slot = helpers.getGuiHelper().getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SellingBinRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, IN_X, IN_Y)
            .addIngredients(recipe.input);

        IRecipeSlotBuilder out = builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X, OUT_Y)
            .addItemStack(SellingBinClientPriceHelper.getPreviewOutput(recipe));

        out.addRichTooltipCallback((recipeSlotView, tooltip) -> {
            TooltipComponent component = new SellingBinTooltipComponent(
                    recipe.input,
                    SellingBinClientPriceHelper.getPreviewOutput(recipe),
                    SellingBinClientPriceHelper.getPriceText(recipe)
            );
            tooltip.add(component);
        });
    }

    @Override
    public void draw(SellingBinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // slot backgrounds
        slot.draw(guiGraphics, IN_X - 1, IN_Y - 1);
        slot.draw(guiGraphics, OUT_X - 1, OUT_Y - 1);

        guiGraphics.renderItem(ModItems.EQUALS.asStack(), SEPARATOR_X, SEPARATOR_Y);
        renderPriceOverlay(guiGraphics, OUT_X, OUT_Y, SellingBinClientPriceHelper.getPriceText(recipe));
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, SellingBinRecipe recipe, IFocusGroup focuses) {
        // no time bar; selling bin runs on a fixed server interval.
    }

    private static void renderPriceOverlay(GuiGraphics guiGraphics, int slotX, int slotY, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        var font = Minecraft.getInstance().font;
        int textWidth = Math.max(1, font.width(text));
        float scale = Math.min(1.0F, 16.0F / textWidth);
        float inverseScale = 1.0F / scale;

        int scaledTextWidth = Math.round(textWidth * scale);
        int drawX = slotX + SLOT_SIZE - 1 - scaledTextWidth + OVERLAY_OFFSET_X;
        int drawY = slotY + SLOT_SIZE - 1 - Math.round(font.lineHeight * scale) + OVERLAY_OFFSET_Y;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, OVERLAY_Z_OFFSET);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, Math.round(drawX * inverseScale), Math.round(drawY * inverseScale), 0xFFFFFF, true);
        guiGraphics.pose().popPose();
    }
}





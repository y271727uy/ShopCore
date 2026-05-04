package com.y271727uy.shopcore.integration.jei.sdmshop.category;

import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopDataBridge;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopCurrencyItems;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopJeiEntry;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SdmShopCategory implements IRecipeCategory<SdmShopJeiEntry> {
    private static final int IN_X = 20;
    private static final int IN_Y = 22;
    private static final int PRICE_X = 58;
    private static final int PRICE_Y = 22;
    private static final int SLOT_SIZE = 18;
    private static final int OVERLAY_Z_OFFSET = 200;
    private static final int OVERLAY_OFFSET_X = 1;
    private static final int OVERLAY_OFFSET_Y = 1;

    private final mezz.jei.api.recipe.RecipeType<SdmShopJeiEntry> recipeType;
    private final Component title = Component.translatable("gui.shopjei.info");
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public SdmShopCategory(IJeiHelpers helpers, mezz.jei.api.recipe.RecipeType<SdmShopJeiEntry> recipeType) {
        this.recipeType = recipeType;
        this.background = helpers.getGuiHelper().createBlankDrawable(150, 50);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(SdmShopCurrencyItems.copperCoin());
        this.slot = helpers.getGuiHelper().getSlotDrawable();
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<SdmShopJeiEntry> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, SdmShopJeiEntry recipe, IFocusGroup focuses) {
        RecipeIngredientRole itemRole = recipe.isSell() ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
        RecipeIngredientRole moneyRole = recipe.isSell() ? RecipeIngredientRole.OUTPUT : RecipeIngredientRole.INPUT;

        builder.addSlot(itemRole, IN_X, IN_Y)
                .addItemStack(recipe.displayStack());
        builder.addSlot(moneyRole, PRICE_X, PRICE_Y)
                .addItemStack(SdmShopCurrencyItems.copperCoin());
    }

    @Override
    public void draw(SdmShopJeiEntry recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics, IN_X - 1, IN_Y - 1);

        guiGraphics.drawString(Minecraft.getInstance().font, recipe.shopName(), 20, 6, 0x404040, false);
        guiGraphics.renderItem(SdmShopCurrencyItems.copperCoin(), PRICE_X, PRICE_Y);
        renderOverlay(guiGraphics, SdmShopDataBridge.getPriceText(recipe), PRICE_X + 18, PRICE_Y);
        guiGraphics.drawString(Minecraft.getInstance().font,
                recipe.locked()
                        ? Component.translatable("gui.shopjei.lock").getString()
                        : Component.translatable(recipe.isSell() ? "gui.shopjei.sell" : "gui.shopjei.buy").getString(),
                20, 36, recipe.locked() ? 0xAA0000 : 0x404040, false);
        if (recipe.locked()) {
            String lockText = recipe.lockReason().isBlank()
                    ? Component.translatable("gui.shopjei.lock_info").getString()
                    : recipe.lockReason();
            guiGraphics.drawString(Minecraft.getInstance().font, lockText, 20, 46, 0xAA0000, false);
        }
    }

    private static void renderOverlay(GuiGraphics guiGraphics, String text, int slotX, int slotY) {
        if (text.isEmpty()) {
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



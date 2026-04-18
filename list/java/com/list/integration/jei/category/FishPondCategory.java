package com.list.integration.jei.category;

import com.list.all.ModBlocks;
import com.list.integration.jei.ListJeiPlugin;
import com.list.recipe.ChancedItemStack;
import com.list.recipe.FishPondRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FishPondCategory extends AbstractRecipeCategory<FishPondRecipe> {
    public static int INPUT_START_X = 1;
    public static int INPUT_START_Y = 15;
    public static int OUTPUT_START_X = 100;
    public static int OUTPUT_START_Y = 15;

    private final IDrawable slot;

    public FishPondCategory(IJeiHelpers helpers) {
        super(
            ListJeiPlugin.FISH_POND,
            Component.translatable("gui.list.category.fish_pond"),
            helpers.getGuiHelper().createDrawableItemStack(ModBlocks.FISHPOND_CORE.asStack()),
            120,
            68
        );
        slot = helpers.getGuiHelper().getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FishPondRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.ingredients.size(); i++) {
            int x = INPUT_START_X + (i % 3) * 18;
            int y = INPUT_START_Y + (i / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .addIngredients(recipe.ingredients.get(i));
        }

        for (int i = 0; i < recipe.results.size(); i++) {
            int x = OUTPUT_START_X;
            int y = OUTPUT_START_Y + i * 18;
            ChancedItemStack chancedItemStack = recipe.results.get(i);
            IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                .addItemStack(chancedItemStack.itemStack());
            if (chancedItemStack.chance() < 1.0f) {
                slotBuilder.addRichTooltipCallback(((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.translatable("gui.list.category.fish_pond.chance", (int) (chancedItemStack.chance() * 100)));
                }));
            }
        }
    }

    @Override
    public void draw(FishPondRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // inputs
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                slot.draw(guiGraphics, INPUT_START_X + x * 18 - 1, INPUT_START_Y + y * 18 - 1);
            }
        }

        // outputs
        for (int y = 0; y < 3; y++) {
            slot.draw(guiGraphics, OUTPUT_START_X - 1, OUTPUT_START_Y + y * 18 - 1);
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, FishPondRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.time)
            .setPosition(65, 34);
        Component text = recipe.isLava ?
            Component.translatable("gui.list.category.fish_pond.require_lava") :
            Component.translatable("gui.list.category.fish_pond.require_water");
        builder.addText(text, 100, 12).setPosition(0, 1);
    }
}

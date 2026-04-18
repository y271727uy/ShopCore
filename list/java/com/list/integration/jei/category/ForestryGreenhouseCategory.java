package com.list.integration.jei.category;

import com.list.all.ModBlocks;
import com.list.integration.jei.ListJeiPlugin;
import com.list.recipe.ForestryGreenhouseRecipe;
import com.list.recipe.HybridizerIngredient;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ForestryGreenhouseCategory extends AbstractRecipeCategory<ForestryGreenhouseRecipe> {

    // Coordinates aligned to ForestryGreenhouseMenu slots.
    // Basic 0-8 at 14/32/50 x 29/47/65
    private static final int[] BASIC_X = {14, 14, 14, 32, 32, 32, 50, 50, 50};
    private static final int[] BASIC_Y = {29, 47, 65, 29, 47, 65, 29, 47, 65};

    // trench slot 10 (index 9)
    private static final int TRENCH_X = 50;
    private static final int TRENCH_Y = 86;

    // water slot 11 (index 10)
    private static final int WATER_X = 70;
    private static final int WATER_Y = 86;

    // fuel slot 12 (index 11)
    private static final int FUEL_X = 90;
    private static final int FUEL_Y = 86;

    // outputs 12-20 at 115/133/151 x 29/47/65
    private static final int[] OUT_X = {115, 133, 151, 115, 133, 151, 115, 133, 151};
    private static final int[] OUT_Y = {29, 29, 29, 47, 47, 47, 65, 65, 65};

    private final IDrawable slot;

    public ForestryGreenhouseCategory(IJeiHelpers helpers) {
        super(
            ListJeiPlugin.FORESTRY_GREENHOUSE,
            Component.translatable("gui.list.category.forestry_greenhouse"),
            helpers.getGuiHelper().createDrawableItemStack(ModBlocks.FORESTRY_GREENHOUSE.asStack()),
            176,
            100
        );
        this.slot = helpers.getGuiHelper().getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForestryGreenhouseRecipe recipe, IFocusGroup focuses) {
        // Basic inputs: shapeless within 1-9, so show the required list spread across the 9 slots.
        for (int i = 0; i < Math.min(recipe.basicInputs.size(), 9); i++) {
            ForestryGreenhouseRecipe.BasicIngredient in = recipe.basicInputs.get(i);
            var item = ForgeRegistries.ITEMS.getValue(in.itemId());
            ItemStack stack = item != null ? new ItemStack(item) : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                stack.setCount(in.count());
            }
            builder.addSlot(RecipeIngredientRole.INPUT, BASIC_X[i], BASIC_Y[i])
                .addItemStack(stack);
        }

        // Trench input (slot 10, index 9)
        if (recipe.trench10 != null) {
            addHybridizerIngredient(builder, recipe.trench10, TRENCH_X, TRENCH_Y);
        }

        // Water requirement display: show water bucket (and optionally a fluid stack)
        if (recipe.water > 0) {
            // JEI supports fluids; show exact amount (mB)
            builder.addSlot(RecipeIngredientRole.INPUT, WATER_X, WATER_Y)
                .addFluidStack(net.minecraft.world.level.material.Fluids.WATER, recipe.water);
        }

        // Fuel requirement display
        if (recipe.fuel) {
            builder.addSlot(RecipeIngredientRole.INPUT, FUEL_X, FUEL_Y)
                // show a couple common fuels
                .addItemStack(new ItemStack(Items.COAL))
                .addItemStack(new ItemStack(Items.CHARCOAL));
        }

        // Outputs
        for (int i = 0; i < Math.min(recipe.outputs.size(), 9); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, OUT_X[i], OUT_Y[i])
                .addItemStack(recipe.outputs.get(i).itemStack());
        }
    }

    private void addHybridizerIngredient(IRecipeLayoutBuilder builder, HybridizerIngredient ingredient, int x, int y) {
        var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, x, y);

        // If render_item is provided, prefer it as the displayed stack.
        ItemStack render = ingredient.renderStack();
        if (render != null && !render.isEmpty()) {
            slotBuilder.addItemStack(render);
            return;
        }

        // Fallback to previews (tag expansions etc) from HybridizerIngredient.
        List<ItemStack> previews = ingredient.previewStacks();
        if (!previews.isEmpty()) {
            slotBuilder.addItemStacks(previews);
        }
    }

    @Override
    public void draw(ForestryGreenhouseRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Inputs slots background
        for (int i = 0; i < 9; i++) {
            slot.draw(guiGraphics, BASIC_X[i] - 1, BASIC_Y[i] - 1);
        }
        // trench/water/fuel backgrounds
        slot.draw(guiGraphics, TRENCH_X - 1, TRENCH_Y - 1);
        slot.draw(guiGraphics, WATER_X - 1, WATER_Y - 1);
        slot.draw(guiGraphics, FUEL_X - 1, FUEL_Y - 1);

        // outputs backgrounds
        for (int i = 0; i < 9; i++) {
            slot.draw(guiGraphics, OUT_X[i] - 1, OUT_Y[i] - 1);
        }

        // Water requirement text (mB)
        if (recipe.water > 0) {
            // Keep the label short so it fits near the water slot.
            guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                "Water: " + recipe.water + " mB",
                WATER_X,
                WATER_Y - 10,
                0x404040,
                false
            );
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ForestryGreenhouseRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.time)
            .setPosition(85, 47);
    }
}







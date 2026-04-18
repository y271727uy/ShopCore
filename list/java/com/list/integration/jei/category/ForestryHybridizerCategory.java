package com.list.integration.jei.category;

import com.list.all.ModBlocks;
import com.list.integration.jei.ListJeiPlugin;
import com.list.recipe.ChancedItemStack;
import com.list.recipe.ForestryHybridizerRecipe;
import com.list.recipe.HybridizerIngredient;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class
ForestryHybridizerCategory extends AbstractRecipeCategory<ForestryHybridizerRecipe> {
    // Coordinates aligned to ForestryHybridizerMenu slots.
    private static final int IN0_X = 29;
    private static final int IN0_Y = 22;
    private static final int IN1_X = 29;
    private static final int IN1_Y = 48;
    private static final int IN2_X = 74;
    private static final int IN2_Y = 36;

    private static final int OUT0_X = 137;
    private static final int OUT0_Y = 22;
    private static final int OUT1_X = 137;
    private static final int OUT1_Y = 48;

    private final IDrawable slot;

    public ForestryHybridizerCategory(IJeiHelpers helpers) {
        super(
            ListJeiPlugin.FORESTRY_HYBRIDIZER,
            Component.translatable("gui.list.category.forestry_hybridizer"),
            helpers.getGuiHelper().createDrawableItemStack(ModBlocks.FORESTRY_HYBRIDIZER.asStack()),
            176,
            80
        );
        this.slot = helpers.getGuiHelper().getSlotDrawable();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForestryHybridizerRecipe recipe, IFocusGroup focuses) {
        // Inputs
        addHybridizerInputSlot(builder, recipe.inputs, 0, IN0_X, IN0_Y);
        addHybridizerInputSlot(builder, recipe.inputs, 1, IN1_X, IN1_Y);
        addHybridizerInputSlot(builder, recipe.inputs, 2, IN2_X, IN2_Y);

        // Outputs
        addChancedOutputSlot(builder, recipe, 0, OUT0_X, OUT0_Y);
        addChancedOutputSlot(builder, recipe, 1, OUT1_X, OUT1_Y);
    }

    private static void addHybridizerInputSlot(IRecipeLayoutBuilder builder, List<HybridizerIngredient> inputs, int index, int x, int y) {
        if (index < 0 || index >= inputs.size()) return;
        HybridizerIngredient ing = inputs.get(index);

        // Prefer explicit render override (strong NBT) to avoid JEI displaying wrong stacks
        // when the real ingredient uses complex/fuzzy NBT.
        ItemStack render = ing.renderStack();
        if (render != null && !render.isEmpty() && render.getItem() != Items.AIR) {
            int count = Math.max(1, ing.count());
            ItemStack display = render.copy();
            display.setCount(Math.max(display.getCount(), count));
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStack(display);
            return;
        }

        List<ItemStack> previews = expandIngredientPreview(ing);
        if (previews.isEmpty()) return;
        builder.addSlot(RecipeIngredientRole.INPUT, x, y).addItemStacks(previews);
    }

    /**
     * JEI 侧展示用：
     * - item: 直接取 previewStacks
     * - tag: 展开 tag 下所有 items
     * - alternatives: 合并上述结果
     */
    private static List<ItemStack> expandIngredientPreview(HybridizerIngredient ing) {
        int count = Math.max(1, ing.count());

        // 先尝试用配方自带 preview（Simple.itemId / Alternatives 中 item 的情况）
        List<ItemStack> base = ing.previewStacks();

        // 如果是 tag（或 alternatives 里含 tag），原实现 previewStacks() 会是空；这里补充 tag 展开
        if (ing instanceof HybridizerIngredient.Simple simple && simple.tagId() != null) {
            base = stacksFromTag(simple.tagId());
            // 如果 Simple 还带 NBT（exact 或 blurry），打到每个 stack 上（展示用途）
            if (simple.nbt() != null && simple.nbtMode() != HybridizerIngredient.NbtMode.NONE) {
                base = base.stream().map(s -> {
                    ItemStack copy = s.copy();
                    copy.setTag(simple.nbt().copy());
                    return copy;
                }).collect(Collectors.toList());
            }
        } else if (ing instanceof HybridizerIngredient.Alternatives alts) {
            base = alts.alternatives().stream()
                .flatMap(s -> {
                    if (s.tagId() != null) {
                        List<ItemStack> stacks = stacksFromTag(s.tagId());
                        if (s.nbt() != null && s.nbtMode() != HybridizerIngredient.NbtMode.NONE) {
                            return stacks.stream().map(st -> {
                                ItemStack copy = st.copy();
                                copy.setTag(s.nbt().copy());
                                return copy;
                            });
                        }
                        return stacks.stream();
                    }
                    return s.previewStacks().stream();
                })
                .collect(Collectors.toList());
        }

        // JEI 会缓存配方对象；这里一定要 copy 再改 count，避免污染原 stack
        return base.stream()
            .filter(s -> !s.isEmpty() && s.getItem() != Items.AIR)
            .map(s -> {
                ItemStack copy = s.copy();
                copy.setCount(Math.max(copy.getCount(), count));
                return copy;
            })
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(Collectors.toList());
    }

    private static List<ItemStack> stacksFromTag(ResourceLocation tagId) {
        TagKey<net.minecraft.world.item.Item> key = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagId);
        var tags = ForgeRegistries.ITEMS.tags();
        if (tags == null) {
            return List.of();
        }
        return tags.getTag(key)
            .stream()
            .filter(Objects::nonNull)
            .map((ItemLike item) -> new ItemStack(item.asItem()))
            .collect(Collectors.toList());
    }

    private static void addChancedOutputSlot(IRecipeLayoutBuilder builder, ForestryHybridizerRecipe recipe, int index, int x, int y) {
        if (index < 0 || index >= recipe.outputs.size()) return;
        ChancedItemStack out = recipe.outputs.get(index);
        ItemStack display = index < recipe.outputDisplayStacks.size()
            ? recipe.outputDisplayStacks.get(index).copy()
            : ItemStack.EMPTY;
        if (display.isEmpty() || display.getItem() == Items.AIR) {
            return;
        }
        IRecipeSlotBuilder slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
            .addItemStack(display);
        if (out.chance() < 1.0f) {
            slotBuilder.addRichTooltipCallback((recipeSlotView, tooltip) ->
                tooltip.add(Component.translatable("gui.list.category.forestry_hybridizer.chance", (int) (out.chance() * 100)))
            );
        }
    }

    @Override
    public void draw(ForestryHybridizerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 3 input slots
        slot.draw(guiGraphics, IN0_X - 1, IN0_Y - 1);
        slot.draw(guiGraphics, IN1_X - 1, IN1_Y - 1);
        slot.draw(guiGraphics, IN2_X - 1, IN2_Y - 1);

        if (recipe.outputs.size() > 0) {
            slot.draw(guiGraphics, OUT0_X - 1, OUT0_Y - 1);
        }
        if (recipe.outputs.size() > 1) {
            slot.draw(guiGraphics, OUT1_X - 1, OUT1_Y - 1);
        }
    }


    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, ForestryHybridizerRecipe recipe, IFocusGroup focuses) {
        builder.addAnimatedRecipeArrow(recipe.time)
            .setPosition(98, 36);
    }
}



package com.list.integration.jade.provider;

import com.list.ListMod;
import com.list.block.entity.FishPondCoreBlockEntity;
import com.list.recipe.FishPondRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.impl.ui.ProgressStyle;

import java.util.List;

public enum FishPondProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final IProgressStyle PROGRESS_STYLE = new ProgressStyle()
        .color(0xFFFFFFFF)
        .textColor(-1)
        .vertical(false);

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig pluginConfig) {
        CompoundTag compoundTag = blockAccessor.getServerData();
        boolean isFormed = compoundTag.getBoolean("isFormed");
        if (!isFormed) {
            tooltip.add(Component.translatable("tooltip.fishpond.not_formed"));
            return;
        }
        tooltip.add(Component.translatable("tooltip.fishpond.formed"));
        boolean isLava = compoundTag.getBoolean("isLava");
        if (isLava) {
            tooltip.add(Component.translatable("tooltip.fishpond.lava_mode"));
        } else {
            tooltip.add(Component.translatable("tooltip.fishpond.water_mode"));
        }
        if (compoundTag.contains("maxProgress")) {
            int maxProgress = compoundTag.getInt("maxProgress");
            int progress = compoundTag.getInt("progress");
            float progressFloat = progress / (float) maxProgress;
            tooltip.add(IElementHelper.get().progress(
                progressFloat,
                Component.literal("%.1f%%".formatted(progressFloat * 100)).withStyle(colorFromRatio(progressFloat, true)),
                PROGRESS_STYLE,
                BoxStyle.DEFAULT,
                false
            ));
        }
        if (compoundTag.contains("outputs")) {
            ListTag outputs = compoundTag.getList("outputs", ListTag.TAG_COMPOUND);
            if (!outputs.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.fishpond.recipe_outputs"));
                for (int i = 0; i < outputs.size(); i++) {
                    ItemStack itemStack = ItemStack.of(outputs.getCompound(i));
                    tooltip.add(Component.literal("- "));
                    tooltip.append(IElementHelper.get().smallItem(itemStack));
                    tooltip.append(Component.literal(itemStack.getCount() + "* ").append(itemStack.getHoverName()));
                }
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (blockAccessor.getBlockEntity() instanceof FishPondCoreBlockEntity blockEntity) {
            compoundTag.putBoolean("isFormed", blockEntity.isFormed());
            compoundTag.putBoolean("isLava", blockEntity.isLava());
            FishPondRecipe runningRecipeCache = blockEntity.getRunningRecipeCache();
            if (runningRecipeCache != null) {
                compoundTag.putInt("maxProgress", runningRecipeCache.time);
                compoundTag.putInt("progress", blockEntity.getProgress());
                ListTag output = new ListTag();
                List<ItemStack> pendingOutputs = blockEntity.getPendingOutputs();
                if (!pendingOutputs.isEmpty()) {
                    for (ItemStack stack : pendingOutputs) {
                        output.add(stack.save(new CompoundTag()));
                    }
                }
                compoundTag.put("outputs", output);
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ListMod.rl("fish_pond");
    }

    public static Style colorFromRatio(double ratio, boolean oneIsGreen) {
        double p = ratio;

        if (!oneIsGreen) {
            p = 1 - p;
        }

        int r = (int) (255d * (Math.max(0, Math.min(2 - 2 * p, 1))));
        int g = (int) (255d * (Math.max(0, Math.min(2 * p, 1))));
        int rgb = 0xFF000000 + (r << 16) + (g << 8);

        return Style.EMPTY.withItalic(false).withColor(TextColor.fromRgb(rgb));
    }
}

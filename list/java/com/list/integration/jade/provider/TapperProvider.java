package com.list.integration.jade.provider;

import com.list.ListMod;
import com.list.block.TapperBlock;
import com.list.block.TapperRecipe;
import com.list.block.entity.TapperBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;
import snownee.jade.impl.ui.ProgressStyle;

public enum TapperProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final IProgressStyle PROGRESS_STYLE = new ProgressStyle()
            .color(0xFFFFFFFF)
            .textColor(-1)
            .vertical(false);

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBoolean("hasRecipe")) {
            tooltip.add(Component.translatable("tooltip.list.tapper.no_recipe"));
            return;
        }


        if (data.getBoolean("mature")) {
            tooltip.add(Component.translatable("tooltip.list.tapper.state.mature"));
        } else {
            tooltip.add(Component.translatable("tooltip.list.tapper.state.working"));
        }

        if (data.contains("outputName")) {
            tooltip.add(Component.translatable("tooltip.list.tapper.output", Component.literal(data.getString("outputName"))));
        }

        if (data.contains("remainingTicks") && data.contains("totalTicks")) {
            int remaining = data.getInt("remainingTicks");
            int total = data.getInt("totalTicks");
            float progress = total <= 0 ? 0.0F : 1.0F - (remaining / (float) total);
            tooltip.add(IElementHelper.get().progress(
                    progress,
                    Component.literal(String.format("%.1f%%", progress * 100.0F)),
                    PROGRESS_STYLE,
                    BoxStyle.DEFAULT,
                    false
            ));
            tooltip.add(Component.translatable("tooltip.list.tapper.remaining", formatTicks(remaining)));
        }

        if (data.contains("toolName")) {
            String toolName = data.getString("toolName");
            if (!toolName.isBlank()) {
                tooltip.add(Component.translatable("tooltip.list.tapper.tool", getItemDisplayName(toolName)));
            } else {
                tooltip.add(Component.translatable("tooltip.list.tapper.tool.none"));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof TapperBlockEntity be)) {
            return;
        }

        TapperRecipe recipe = TapperBlock.getAttachedRecipe(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState().getValue(TapperBlock.FACING));
        if (recipe == null) {
            tag.putBoolean("hasRecipe", false);
            return;
        }

        tag.putBoolean("hasRecipe", true);
        tag.putBoolean("mature", accessor.getBlockState().getValue(TapperBlock.MATURE));
        tag.putString("outputName", TapperRecipe.parseOutput(recipe.output).getHoverName().getString());
        tag.putString("toolName", recipe.tool);

        int totalTicks = recipe.time;
        int remainingTicks = totalTicks;
        if (be.startTime > 0L) {
            long elapsed = accessor.getLevel().getGameTime() - be.startTime;
            remainingTicks = (int) Math.max(0L, (long) totalTicks - elapsed);
        }
        tag.putInt("totalTicks", totalTicks);
        tag.putInt("remainingTicks", remainingTicks);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getUid() {
        return ListMod.rl("tapper");
    }

    private static String formatTicks(int ticks) {
        int seconds = Math.max(0, ticks) / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return (minutes > 0 ? minutes + "分" : "") + seconds + "秒";
    }

    private static Component getItemDisplayName(String itemId) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl == null) {
            return Component.literal(itemId);
        }
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) {
            return Component.literal(itemId);
        }
        return item.getDescription().copy();
    }
}




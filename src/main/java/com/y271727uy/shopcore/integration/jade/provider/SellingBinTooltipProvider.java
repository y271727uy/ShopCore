package com.y271727uy.shopcore.integration.jade.provider;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.block.entity.SellingBinBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum SellingBinTooltipProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "selling_bin_jade");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (!(blockEntity instanceof SellingBinBlockEntity sellingBin)) {
            return;
        }

        boolean bound = sellingBin.isBound();
        if (bound) {
            Component accountType = Component.translatable(sellingBin.isTaxExempt()
                    ? "tooltip.shopcore.jade.selling_bin.account_premium"
                    : "tooltip.shopcore.jade.selling_bin.account_normal").withStyle(sellingBin.isTaxExempt() ? ChatFormatting.GOLD : ChatFormatting.GREEN);
            tooltip.add(Component.translatable("tooltip.shopcore.jade.selling_bin.binding_account", accountType).withStyle(ChatFormatting.GRAY));
        } else {
            Component state = Component.translatable("tooltip.shopcore.jade.selling_bin.unbound").withStyle(ChatFormatting.RED);
            tooltip.add(Component.translatable("tooltip.shopcore.jade.selling_bin.status", state).withStyle(ChatFormatting.GRAY));
        }

        if (!bound) {
            return;
        }

        String boundPlayerName = sellingBin.getBoundPlayerName();
        Component playerComponent = (boundPlayerName == null || boundPlayerName.isBlank())
                ? Component.translatable("tooltip.shopcore.jade.selling_bin.player_unknown")
                : Component.literal(boundPlayerName);
        tooltip.add(Component.translatable("tooltip.shopcore.jade.selling_bin.player", playerComponent).withStyle(ChatFormatting.GRAY));
    }
}


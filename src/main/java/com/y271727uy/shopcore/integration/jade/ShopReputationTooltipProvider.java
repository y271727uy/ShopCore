package com.y271727uy.shopcore.integration.jade;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.api.shop.ShopcoreShops;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Generic Jade provider using the public shop query API.
 *
 * Displays:
 * - Shop open/closed status
 * - Current reputation
 * - Active orders count
 */
public enum ShopReputationTooltipProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(
        ShopcoreMod.MODID,
        "shop_reputation_jade"
    );

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();

        // Check if this block entity is a shop
        if (!ShopcoreShops.isShop(blockEntity)) {
            return;
        }

        var snapshot = ShopcoreShops.snapshot(blockEntity);
        if (snapshot.isEmpty()) {
            // Shop not initialized yet
            tooltip.add(Component.translatable("tooltip.shopcore.shop.uninitialized")
                .withStyle(ChatFormatting.GRAY));
            return;
        }
        var shop = snapshot.get();

        // Shop status
        if (shop.isOpen()) {
            tooltip.add(Component.translatable("tooltip.shopcore.shop.status.open")
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.shopcore.shop.status.closed")
                .withStyle(ChatFormatting.RED));
        }

        // Reputation
        double reputation = shop.reputation();
        ChatFormatting reputationColor = getReputationColor(reputation);
        String reputationTier = getReputationTier(reputation);

        tooltip.add(Component.translatable(
            "tooltip.shopcore.shop.reputation",
            String.format("%.1f", reputation),
            Component.translatable(reputationTier)
        ).withStyle(reputationColor));

        // Active orders
        if (shop.activeOrderCount() > 0) {
            tooltip.add(Component.translatable(
                "tooltip.shopcore.shop.active_orders",
                shop.activeOrderCount()
            ).withStyle(ChatFormatting.YELLOW));
        }
    }

    /**
     * Get color based on reputation level
     */
    private static ChatFormatting getReputationColor(double reputation) {
        if (reputation >= 1000) return ChatFormatting.GOLD;        // Legendary
        if (reputation >= 500) return ChatFormatting.LIGHT_PURPLE; // Master
        if (reputation >= 200) return ChatFormatting.BLUE;         // Expert
        if (reputation >= 50) return ChatFormatting.GREEN;         // Skilled
        return ChatFormatting.GRAY;                                // Novice
    }

    /**
     * Get reputation tier translation key
     */
    private static String getReputationTier(double reputation) {
        if (reputation >= 1000) return "tooltip.shopcore.shop.reputation.tier.legendary";
        if (reputation >= 500) return "tooltip.shopcore.shop.reputation.tier.master";
        if (reputation >= 200) return "tooltip.shopcore.shop.reputation.tier.expert";
        if (reputation >= 50) return "tooltip.shopcore.shop.reputation.tier.skilled";
        return "tooltip.shopcore.shop.reputation.tier.novice";
    }
}

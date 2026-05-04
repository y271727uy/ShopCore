package com.y271727uy.shopcore.integration.jei.sdmshop;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class SdmShopUIUtils {
    private SdmShopUIUtils() {
    }

    public static List<SdmShopJeiEntry> getAllShopItems() {
        SdmShopDataBridge.refreshEntries();
        return SdmShopDataBridge.snapshot();
    }

    public static Optional<SdmShopJeiEntry> getShopItemByStack(ItemStack stack) {
        return Optional.ofNullable(SdmShopDataBridge.findByStack(stack));
    }

    public static void openShopGui(SdmShopJeiEntry entry) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (SdmShopRuntimeBridge.tryOpenCurrentScreen(entry.displayStack())) {
            return;
        }

        minecraft.player.displayClientMessage(Component.literal("SDM shop entry: " + entry.shopName()), false);
    }

    public static String formatPrice(int price, String currency) {
        return Integer.toString(Math.max(0, price));
    }

    public static String formatPrice(SdmShopJeiEntry entry) {
        return entry.priceText();
    }
}




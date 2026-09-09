package com.y271727uy.shopcore.client;

import com.y271727uy.shopcore.all.ModItem;
import com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopCurrencyItems;
import com.y271727uy.shopcore.integration.sdm.SdmCurrencyHelperBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalDouble;

/** Renders the SDM balance beside the hotbar when both bank card variants are carried. */
public final class BankBalanceHudRenderer {
    private BankBalanceHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !hasBankCard(minecraft.player.getInventory())) {
            return;
        }

        OptionalDouble balance = SdmCurrencyHelperBridge.queryBalance(minecraft.player);
        ItemStack coin = SdmShopCurrencyItems.copperCoin();
        if (balance.isEmpty() || coin.isEmpty()) {
            return;
        }

        int hotbarLeft = graphics.guiWidth() / 2 - 91;
        long roundedBalance = Math.max(0L, Math.round(balance.getAsDouble()));
        String amount = compactAmount(roundedBalance);
        int amountWidth = minecraft.font.width(amount);
        int groupWidth = 16 + 3 + amountWidth;
        // Keep the complete group outside the first hotbar slot.
        int x = Math.max(2, hotbarLeft - groupWidth - 4);
        int y = graphics.guiHeight() - 20;
        graphics.renderItem(coin, x, y);
        graphics.drawString(minecraft.font, amount, x + 19, y + 4, 0xFFFFFFFF, true);
    }

    private static String compactAmount(long amount) {
        if (amount >= 1_000_000_000L) {
            return String.format("%.1fB", amount / 1_000_000_000D);
        }
        if (amount >= 1_000_000L) {
            return String.format("%.1fM", amount / 1_000_000D);
        }
        if (amount >= 1_000L) {
            return String.format("%.1fK", amount / 1_000D);
        }
        return Long.toString(amount);
    }

    private static boolean hasBankCard(Inventory inventory) {
        for (ItemStack stack : inventory.items) {
            if (stack.is(ModItem.BANK_CARD.get())) {
                return true;
            }
            if (stack.is(ModItem.PREMIUM_BANK_CARD.get())) {
                return true;
            }
        }
        return false;
    }
}

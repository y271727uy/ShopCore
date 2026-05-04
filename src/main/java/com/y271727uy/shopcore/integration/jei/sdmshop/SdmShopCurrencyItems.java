package com.y271727uy.shopcore.integration.jei.sdmshop;

import com.y271727uy.shopcore.economic.CurrencyDenomination;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SdmShopCurrencyItems {
    private SdmShopCurrencyItems() {
    }

    public static ItemStack copperCoin() {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("list", CurrencyDenomination.COPPER_GT_CREDIT.itemPath());
        return ForgeRegistries.ITEMS.getValue(location) == null ? ItemStack.EMPTY : new ItemStack(ForgeRegistries.ITEMS.getValue(location));
    }

    public static String copperCoinLabel() {
        ItemStack stack = copperCoin();
        return stack.isEmpty() ? "list:copper_coin" : stack.getDescriptionId();
    }
}





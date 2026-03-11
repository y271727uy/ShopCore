package com.y271727uy.shopcore.economic;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * Default mapping from logical currency denominations to real list:* currency items.
 */
public final class DefaultCurrencyStackFactory implements CurrencyStackFactory {
    public static final DefaultCurrencyStackFactory INSTANCE = new DefaultCurrencyStackFactory();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LIST_NAMESPACE = "list";

    private DefaultCurrencyStackFactory() {
    }

    @Override
    public ItemStack createStack(CurrencyDenomination denomination, int amount) {
        Objects.requireNonNull(denomination, "denomination");
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }

        ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath(LIST_NAMESPACE, denomination.itemPath());
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            LOGGER.warn("Missing currency item for denomination {} at id {}", denomination, itemId);
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, amount);
    }
}



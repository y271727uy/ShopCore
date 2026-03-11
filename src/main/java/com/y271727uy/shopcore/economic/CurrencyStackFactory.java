package com.y271727uy.shopcore.economic;

import net.minecraft.world.item.ItemStack;

/**
 * Converts logical currency payouts into actual item stacks.
 * Checkout logic stays decoupled from concrete currency item implementations.
 */
@FunctionalInterface
public interface CurrencyStackFactory {
    ItemStack createStack(CurrencyDenomination denomination, int amount);
}


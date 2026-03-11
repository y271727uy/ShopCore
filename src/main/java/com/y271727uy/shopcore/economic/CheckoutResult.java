package com.y271727uy.shopcore.economic;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Final settlement output returned by {@link CustomerCheckout}.
 *
 * @param currency currency stacks, if a concrete factory is supplied
 * @param payouts logical denomination split, always available
 * @param totalValue final rounded currency value paid out
 * @param reputation final reputation reward
 */
public record CheckoutResult(List<ItemStack> currency, List<CurrencyPayout> payouts, long totalValue, double reputation) {
    public CheckoutResult {
        currency = List.copyOf(currency);
        payouts = List.copyOf(payouts);
    }
}


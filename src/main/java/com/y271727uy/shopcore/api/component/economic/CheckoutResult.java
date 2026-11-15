package com.y271727uy.shopcore.api.component.economic;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Settlement output. Item stacks are copied on input and access; logical payouts are immutable. */
public record CheckoutResult(List<ItemStack> currency, List<CurrencyPayout> payouts, long totalValue, double reputation) {
    public CheckoutResult {
        currency = currency.stream().map(ItemStack::copy).toList();
        payouts = List.copyOf(payouts);
    }

    @Override
    public List<ItemStack> currency() {
        return currency.stream().map(ItemStack::copy).toList();
    }
}

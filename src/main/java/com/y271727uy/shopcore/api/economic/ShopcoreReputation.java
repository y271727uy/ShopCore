package com.y271727uy.shopcore.api.economic;

import com.y271727uy.shopcore.economic.CheckoutInput;
import com.y271727uy.shopcore.economic.Price;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * Public API for reputation calculation.
 * Reputation always originates from {@link Price} data and is combined through addition.
 */
public final class ShopcoreReputation {
    private ShopcoreReputation() {
    }

    public static double getReputation(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return ShopcorePrices.getPrice(stack).reputation();
    }

    public static double sumReputation(List<ItemStack> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        return stacks.stream()
                .filter(Objects::nonNull)
                .mapToDouble(ShopcoreReputation::getReputation)
                .sum();
    }

    public static double sumReputationFromPrices(List<Price> prices) {
        Objects.requireNonNull(prices, "prices");
        return prices.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Price::reputation)
                .sum();
    }

    public static double calculateCheckoutReputation(CheckoutInput input) {
        Objects.requireNonNull(input, "input");
        return input.finalReputation();
    }

    public static double calculateCheckoutReputation(Price summarizedPrice, int quantity, double multiplier) {
        Objects.requireNonNull(summarizedPrice, "summarizedPrice");
        return calculateCheckoutReputation(new CheckoutInput(
                summarizedPrice.basicPrice(),
                summarizedPrice.addPrice(),
                summarizedPrice.reputation(),
                quantity,
                multiplier
        ));
    }
}


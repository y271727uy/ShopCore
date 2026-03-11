package com.y271727uy.shopcore.economic;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure settlement module.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Read summarized order values</li>
 *     <li>Compute final settlement value</li>
 *     <li>Split settlement value into currency payouts</li>
 *     <li>Return currency and reputation rewards</li>
 * </ul>
 * It deliberately does not care about customer generation, order generation,
 * or item price lookup.
 */
public final class CustomerCheckout {
	private CustomerCheckout() {
	}

	public static CheckoutResult checkout(CheckoutInput input) {
		return checkout(input, null);
	}

	public static CheckoutResult checkout(CheckoutInput input, CurrencyStackFactory stackFactory) {
		Objects.requireNonNull(input, "input");

		long totalValue = input.finalPriceRounded();
		List<CurrencyPayout> payouts = splitCurrency(totalValue);
		List<ItemStack> currencyStacks = createCurrencyStacks(payouts, stackFactory);

		return new CheckoutResult(currencyStacks, payouts, totalValue, input.finalReputation());
	}

	public static CheckoutResult checkout(Price summarizedPrice, int quantity, double multiplier) {
		Objects.requireNonNull(summarizedPrice, "summarizedPrice");
		return checkout(new CheckoutInput(
				summarizedPrice.basicPrice(),
				summarizedPrice.addPrice(),
				summarizedPrice.reputation(),
				quantity,
				multiplier
		));
	}

	public static List<CurrencyPayout> splitCurrency(long totalValue) {
		if (totalValue < 0) {
			throw new IllegalArgumentException("totalValue cannot be negative");
		}

		List<CurrencyPayout> payouts = new ArrayList<>();
		long remaining = totalValue;

		for (CurrencyDenomination denomination : CurrencyDenomination.descendingValues()) {
			if (remaining < denomination.value()) {
				continue;
			}

			int amount = (int) (remaining / denomination.value());
			payouts.add(new CurrencyPayout(denomination, amount));
			remaining -= (long) amount * denomination.value();
		}

		return payouts;
	}

	private static List<ItemStack> createCurrencyStacks(List<CurrencyPayout> payouts, CurrencyStackFactory stackFactory) {
		if (stackFactory == null) {
			return List.of();
		}

		List<ItemStack> currencyStacks = new ArrayList<>();
		for (CurrencyPayout payout : payouts) {
			ItemStack stack = stackFactory.createStack(payout.denomination(), payout.amount());
			if (stack != null && !stack.isEmpty()) {
				currencyStacks.add(stack);
			}
		}
		return currencyStacks;
	}
}

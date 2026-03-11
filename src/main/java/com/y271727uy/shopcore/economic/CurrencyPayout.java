package com.y271727uy.shopcore.economic;

/**
 * One denomination payout entry produced by the greedy settlement algorithm.
 *
 * @param denomination currency denomination
 * @param amount how many of that denomination should be paid out
 */
public record CurrencyPayout(CurrencyDenomination denomination, int amount) {
    public CurrencyPayout {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
    }

    public long totalValue() {
        return denomination.value() * amount;
    }
}


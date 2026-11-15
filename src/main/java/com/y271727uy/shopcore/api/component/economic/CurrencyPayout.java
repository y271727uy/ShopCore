package com.y271727uy.shopcore.api.component.economic;

import java.util.Objects;

/**
 * One denomination payout entry produced by the greedy settlement algorithm.
 *
 * @param denomination currency denomination
 * @param amount how many of that denomination should be paid out
 */
public record CurrencyPayout(CurrencyDenomination denomination, int amount) {
    public CurrencyPayout {
        Objects.requireNonNull(denomination, "denomination");
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
    }

    public long totalValue() {
        return denomination.value() * amount;
    }
}


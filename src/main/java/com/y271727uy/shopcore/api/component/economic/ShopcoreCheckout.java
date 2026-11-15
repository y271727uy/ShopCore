package com.y271727uy.shopcore.api.component.economic;

import com.y271727uy.shopcore.core.economic.checkout.CustomerCheckout;
import com.y271727uy.shopcore.core.economic.currency.DefaultCurrencyStackFactory;

import java.util.Objects;

/** Calculates rewards and optionally creates currency items. It does not deposit into an account. */
public final class ShopcoreCheckout {
    private ShopcoreCheckout() { }

    /** Calculates denomination payouts and reputation without creating item stacks. */
    public static CheckoutResult calculate(CheckoutInput input) {
        return CustomerCheckout.checkout(input);
    }

    /** Creates the default currency items as well as the logical payout. */
    public static CheckoutResult checkout(CheckoutInput input) {
        return checkout(input, DefaultCurrencyStackFactory.INSTANCE);
    }

    public static CheckoutResult checkout(CheckoutInput input, CurrencyStackFactory stackFactory) {
        return CustomerCheckout.checkout(input, Objects.requireNonNull(stackFactory, "stackFactory"));
    }
}

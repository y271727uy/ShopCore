package com.y271727uy.shopcore.api.economic;

import com.y271727uy.shopcore.economic.checkout.CheckoutInput;
import com.y271727uy.shopcore.economic.checkout.CheckoutResult;
import com.y271727uy.shopcore.economic.currency.CurrencyStackFactory;
import com.y271727uy.shopcore.economic.checkout.CustomerCheckout;
import com.y271727uy.shopcore.economic.currency.DefaultCurrencyStackFactory;
import com.y271727uy.shopcore.economic.price.Price;


public final class ShopcoreCheckout {
    private ShopcoreCheckout() {
    }

    public static CheckoutResult checkout(CheckoutInput input) {
        return CustomerCheckout.checkout(input, DefaultCurrencyStackFactory.INSTANCE);
    }

    public static CheckoutResult checkout(CheckoutInput input, CurrencyStackFactory stackFactory) {
        return CustomerCheckout.checkout(input, stackFactory);
    }

    public static CheckoutResult checkout(Price summarizedPrice, int quantity, double multiplier) {
        return checkout(new CheckoutInput(
                summarizedPrice.basicPrice(),
                summarizedPrice.addPrice(),
                summarizedPrice.reputation(),
                quantity,
                multiplier
        ));
    }

    public static double checkoutReputation(CheckoutInput input) {
        return ShopcoreReputation.calculateCheckoutReputation(input);
    }

    public static double checkoutReputation(Price summarizedPrice, int quantity, double multiplier) {
        return ShopcoreReputation.calculateCheckoutReputation(summarizedPrice, quantity, multiplier);
    }
}



package com.y271727uy.shopcore.economic;

/**
 * Summarized order data consumed by {@link CustomerCheckout}.
 * It deliberately excludes customer, order generation, and item lookup concerns.
 *
 * @param basicPrice total basic price already aggregated by the order system
 * @param addPrice total additional price already aggregated by the order system
 * @param reputation total reputation already aggregated by the order system
 * @param quantity how many copies of the summarized order are being settled
 * @param multiplier final settlement coefficient
 */
public record CheckoutInput(double basicPrice, double addPrice, double reputation, int quantity, double multiplier) {
    public CheckoutInput {
        if (basicPrice < 0) {
            throw new IllegalArgumentException("basicPrice cannot be negative");
        }
        if (addPrice < 0) {
            throw new IllegalArgumentException("addPrice cannot be negative");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("quantity must be at least 1");
        }
        if (multiplier < 0) {
            throw new IllegalArgumentException("multiplier cannot be negative");
        }
    }

    public double unitPrice() {
        return basicPrice + addPrice;
    }

    public double finalPrice() {
        return unitPrice() * quantity * multiplier;
    }

    public double finalReputation() {
        return reputation * quantity * multiplier;
    }

    public long finalPriceRounded() {
        return Math.round(finalPrice());
    }
}


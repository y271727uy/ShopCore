package com.y271727uy.shopcore.economic;

/**
 * Immutable price data for economic simulation.
 *
 * @param basicPrice base value of the item
 * @param addPrice extra value applied on top of the base value
 * @param reputation reputation value associated with the item
 */
public record Price(int basicPrice, int addPrice, int reputation) {

	public static final Price EMPTY = new Price(0, 0, 0);

	public Price {
		if (basicPrice < 0) {
			throw new IllegalArgumentException("basicPrice cannot be negative");
		}
		if (addPrice < 0) {
			throw new IllegalArgumentException("addPrice cannot be negative");
		}
	}

	public static Price of(int basicPrice, int addPrice, int reputation) {
		return new Price(basicPrice, addPrice, reputation);
	}

	public int totalPrice() {
		return basicPrice + addPrice;
	}

	public boolean isEmpty() {
		return this.equals(EMPTY);
	}
}

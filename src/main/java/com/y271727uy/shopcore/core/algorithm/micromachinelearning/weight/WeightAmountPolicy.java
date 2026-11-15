package com.y271727uy.shopcore.core.algorithm.micromachinelearning.weight;

/**
 * Converts an observed domain object into a weight increment.
 *
 * @param <T> observed object type
 */
public interface WeightAmountPolicy<T> {
    double amountOf(T value);
}

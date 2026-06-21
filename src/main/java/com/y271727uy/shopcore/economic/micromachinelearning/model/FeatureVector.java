package com.y271727uy.shopcore.economic.micromachinelearning.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Dense feature vector for tiny online models.
 */
public record FeatureVector(double[] values) {
    public FeatureVector {
        Objects.requireNonNull(values, "values");
        values = values.clone();
        if (values.length == 0) {
            throw new IllegalArgumentException("values cannot be empty");
        }
        for (double value : values) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("values must be finite");
            }
        }
    }

    public static FeatureVector of(double... values) {
        return new FeatureVector(values);
    }

    public int size() {
        return values.length;
    }

    public double valueAt(int index) {
        return values[index];
    }

    @Override
    public double[] values() {
        return values.clone();
    }

    @Override
    public String toString() {
        return "FeatureVector" + Arrays.toString(values);
    }
}

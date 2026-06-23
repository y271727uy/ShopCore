package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Tiny online linear regressor trained one sample at a time with SGD.
 */
public final class OnlineLinearRegressor implements OnlineModel {
    private final LinearModelConfig config;
    private final double[] weights;
    private double bias;

    public OnlineLinearRegressor(int featureCount) {
        this(featureCount, LinearModelConfig.DEFAULT);
    }

    public OnlineLinearRegressor(int featureCount, LinearModelConfig config) {
        if (featureCount <= 0) {
            throw new IllegalArgumentException("featureCount must be positive");
        }
        this.config = Objects.requireNonNull(config, "config");
        this.weights = new double[featureCount];
    }

    @Override
    public double predict(FeatureVector features) {
        requireSize(features);
        double result = bias;
        for (int index = 0; index < weights.length; index++) {
            result += weights[index] * features.valueAt(index);
        }
        return result;
    }

    /**
     * Learns from one sample and returns squared error before the update.
     */
    @Override
    public double learn(LearningSample sample) {
        Objects.requireNonNull(sample, "sample");
        requireSize(sample.features());
        if (sample.weight() == 0.0D) {
            double error = predict(sample.features()) - sample.target();
            return error * error;
        }

        double prediction = predict(sample.features());
        double error = prediction - sample.target();
        double step = config.learningRate() * sample.weight();

        for (int index = 0; index < weights.length; index++) {
            double gradient = error * sample.features().valueAt(index) + config.l2Penalty() * weights[index];
            weights[index] = clamp(weights[index] - step * gradient);
        }
        bias = clamp(bias - step * error);
        return error * error;
    }

    public int featureCount() {
        return weights.length;
    }

    public double bias() {
        return bias;
    }

    public double weightAt(int index) {
        return weights[index];
    }

    public double[] weights() {
        return weights.clone();
    }

    private void requireSize(FeatureVector features) {
        Objects.requireNonNull(features, "features");
        if (features.size() != weights.length) {
            throw new IllegalArgumentException("expected " + weights.length + " features but got " + features.size());
        }
    }

    private double clamp(double value) {
        return Math.max(-config.maxAbsoluteWeight(), Math.min(config.maxAbsoluteWeight(), value));
    }

    @Override
    public String toString() {
        return "OnlineLinearRegressor{bias=" + bias + ", weights=" + Arrays.toString(weights) + "}";
    }
}

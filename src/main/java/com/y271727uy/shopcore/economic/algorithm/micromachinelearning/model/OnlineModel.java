package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model;

public interface OnlineModel {
    double predict(FeatureVector features);

    double learn(LearningSample sample);
}

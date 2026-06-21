package com.y271727uy.shopcore.economic.micromachinelearning.model;

public interface OnlineModel {
    double predict(FeatureVector features);

    double learn(LearningSample sample);
}

package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model;

import java.util.List;
import java.util.Objects;

/**
 * Tiny score estimator that chooses the candidate with the lowest predicted score.
 *
 * @param <T> candidate value type
 */
public final class ScoreDrivenSelector<T> {
    private final OnlineLinearRegressor model;

    public ScoreDrivenSelector(int featureCount) {
        this(new OnlineLinearRegressor(featureCount));
    }

    public ScoreDrivenSelector(int featureCount, LinearModelConfig config) {
        this(new OnlineLinearRegressor(featureCount, config));
    }

    public ScoreDrivenSelector(OnlineLinearRegressor model) {
        this.model = Objects.requireNonNull(model, "model");
    }

    public ScoreChoice<T> chooseLowestScore(List<ScoreCandidate<T>> candidates) {
        Objects.requireNonNull(candidates, "candidates");
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("candidates cannot be empty");
        }

        ScoreCandidate<T> bestCandidate = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (ScoreCandidate<T> candidate : candidates) {
            Objects.requireNonNull(candidate, "candidate");
            double predictedScore = predictScore(candidate.features());
            if (bestCandidate == null || predictedScore < bestScore) {
                bestCandidate = candidate;
                bestScore = predictedScore;
            }
        }
        return new ScoreChoice<>(bestCandidate.value(), bestScore);
    }

    public double predictScore(FeatureVector features) {
        return model.predict(features);
    }

    /**
     * Learns the actual score for a candidate and returns squared error before the update.
     */
    public double learnScore(ScoreCandidate<T> candidate, double actualScore) {
        Objects.requireNonNull(candidate, "candidate");
        if (!Double.isFinite(actualScore)) {
            throw new IllegalArgumentException("actualScore must be finite");
        }
        return model.learn(LearningSample.of(candidate.features(), actualScore));
    }

    public OnlineLinearRegressor model() {
        return model;
    }
}

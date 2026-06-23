package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.weight;

import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model.LinearModelConfig;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model.ScoreCandidate;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model.ScoreChoice;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.model.ScoreDrivenSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Experimental helper that learns which sell-price penalty multiplier tends to produce a lower score.
 */
public final class WeightPenaltyScoreModel {
    private static final double[] DEFAULT_OFFSETS = {-0.10D, -0.05D, 0.0D, 0.05D, 0.10D};

    private final ScoreDrivenSelector<Double> selector;

    public WeightPenaltyScoreModel() {
        this(new ScoreDrivenSelector<>(WeightPenaltyFeatures.FEATURE_COUNT));
    }

    public WeightPenaltyScoreModel(LinearModelConfig config) {
        this(new ScoreDrivenSelector<>(WeightPenaltyFeatures.FEATURE_COUNT, config));
    }

    public WeightPenaltyScoreModel(ScoreDrivenSelector<Double> selector) {
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    public ScoreChoice<Double> chooseMultiplier(WeightSnapshot snapshot, double soldAmount) {
        return chooseMultiplier(snapshot, soldAmount, defaultCandidates(snapshot));
    }

    public ScoreChoice<Double> chooseMultiplier(
            WeightSnapshot snapshot,
            double soldAmount,
            List<Double> candidateMultipliers
    ) {
        Objects.requireNonNull(candidateMultipliers, "candidateMultipliers");
        if (candidateMultipliers.isEmpty()) {
            throw new IllegalArgumentException("candidateMultipliers cannot be empty");
        }

        List<ScoreCandidate<Double>> candidates = new ArrayList<>(candidateMultipliers.size());
        for (double candidateMultiplier : candidateMultipliers) {
            candidates.add(candidate(snapshot, soldAmount, candidateMultiplier));
        }
        return selector.chooseLowestScore(candidates);
    }

    /**
     * Learns the actual score produced by a multiplier. Lower actualScore means a better outcome.
     */
    public double learnMultiplierScore(
            WeightSnapshot snapshot,
            double soldAmount,
            double usedMultiplier,
            double actualScore
    ) {
        return selector.learnScore(candidate(snapshot, soldAmount, usedMultiplier), actualScore);
    }

    public ScoreDrivenSelector<Double> selector() {
        return selector;
    }

    private ScoreCandidate<Double> candidate(WeightSnapshot snapshot, double soldAmount, double candidateMultiplier) {
        return new ScoreCandidate<>(
                candidateMultiplier,
                WeightPenaltyFeatures.of(snapshot, soldAmount, candidateMultiplier)
        );
    }

    private List<Double> defaultCandidates(WeightSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<Double> candidates = new ArrayList<>(DEFAULT_OFFSETS.length);
        double base = snapshot.penaltyMultiplier();
        for (double offset : DEFAULT_OFFSETS) {
            double candidate = clamp(base + offset);
            if (!candidates.contains(candidate)) {
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}

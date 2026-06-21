package com.y271727uy.shopcore.economic.micromachinelearning.helper;

import com.y271727uy.shopcore.economic.micromachinelearning.model.ScoreChoice;
import com.y271727uy.shopcore.economic.micromachinelearning.weight.DefaultPlayerWeightSpace;
import com.y271727uy.shopcore.economic.micromachinelearning.weight.PlayerWeightSpace;
import com.y271727uy.shopcore.economic.micromachinelearning.weight.SellPriceWeightModifier;
import com.y271727uy.shopcore.economic.micromachinelearning.weight.WeightPenaltyScoreModel;
import com.y271727uy.shopcore.economic.micromachinelearning.weight.WeightSnapshot;

import java.util.Map;
import java.util.Objects;

/**
 * Facade for external modules that need player-scoped sell-price pressure.
 *
 * @param <P> player key type
 * @param <K> item or strategy key type
 */
public final class MicroPriceAdjustmentHelper<P, K> {
    private final MicroPriceAdjustmentConfig config;
    private final PlayerWeightSpace<P, K> weightSpace;
    private final WeightPenaltyScoreModel scoreModel;

    public MicroPriceAdjustmentHelper() {
        this(MicroPriceAdjustmentConfig.DEFAULT);
    }

    public MicroPriceAdjustmentHelper(MicroPriceAdjustmentConfig config) {
        this(
                config,
                new DefaultPlayerWeightSpace<>(Objects.requireNonNull(config, "config").weightConfig()),
                new WeightPenaltyScoreModel()
        );
    }

    public MicroPriceAdjustmentHelper(
            MicroPriceAdjustmentConfig config,
            PlayerWeightSpace<P, K> weightSpace,
            WeightPenaltyScoreModel scoreModel
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.weightSpace = Objects.requireNonNull(weightSpace, "weightSpace");
        this.scoreModel = Objects.requireNonNull(scoreModel, "scoreModel");
    }

    /**
     * Records this sale pressure and returns the adjusted sell price.
     */
    public PriceAdjustmentResult adjustSellPrice(PriceAdjustmentRequest<P, K> request) {
        Objects.requireNonNull(request, "request");

        WeightSnapshot before = weightSpace.snapshot(request.playerKey(), request.itemKey(), request.nowTick());
        weightSpace.record(request.playerKey(), request.itemKey(), request.weightAmount(), request.nowTick());
        WeightSnapshot after = weightSpace.snapshot(request.playerKey(), request.itemKey(), request.nowTick());

        double multiplier = chooseMultiplier(after, request.weightAmount());
        int adjustedPrice = applyMultiplier(request.baseSellPrice(), multiplier);
        return new PriceAdjustmentResult(
                request.baseSellPrice(),
                adjustedPrice,
                multiplier,
                before,
                after,
                config.useScoreModel()
        );
    }

    /**
     * Returns the current adjustment without recording new sale pressure.
     */
    public PriceAdjustmentResult previewSellPrice(P playerKey, K itemKey, int baseSellPrice, long nowTick) {
        Objects.requireNonNull(playerKey, "playerKey");
        Objects.requireNonNull(itemKey, "itemKey");
        if (baseSellPrice < 0) {
            throw new IllegalArgumentException("baseSellPrice cannot be negative");
        }

        WeightSnapshot snapshot = weightSpace.snapshot(playerKey, itemKey, nowTick);
        double multiplier = chooseMultiplier(snapshot, 0.0D);
        return new PriceAdjustmentResult(
                baseSellPrice,
                applyMultiplier(baseSellPrice, multiplier),
                multiplier,
                snapshot,
                snapshot,
                config.useScoreModel()
        );
    }

    /**
     * Teaches the optional score model with a module-defined actual score. Lower score means better.
     */
    public double learn(PriceAdjustmentFeedback feedback) {
        Objects.requireNonNull(feedback, "feedback");
        return scoreModel.learnMultiplierScore(
                feedback.weightSnapshot(),
                feedback.weightAmount(),
                feedback.usedMultiplier(),
                feedback.actualScore()
        );
    }

    public WeightSnapshot weightOf(P playerKey, K itemKey, long nowTick) {
        return weightSpace.snapshot(playerKey, itemKey, nowTick);
    }

    public Map<K, WeightSnapshot> weightsOf(P playerKey, long nowTick) {
        return weightSpace.snapshots(playerKey, nowTick);
    }

    public void clear(P playerKey) {
        weightSpace.clear(playerKey);
    }

    public void clear(P playerKey, K itemKey) {
        weightSpace.clear(playerKey, itemKey);
    }

    public PlayerWeightSpace<P, K> weightSpace() {
        return weightSpace;
    }

    public WeightPenaltyScoreModel scoreModel() {
        return scoreModel;
    }

    private double chooseMultiplier(WeightSnapshot snapshot, double weightAmount) {
        if (!config.useScoreModel()) {
            return SellPriceWeightModifier.multiplier(snapshot);
        }

        ScoreChoice<Double> choice = scoreModel.chooseMultiplier(snapshot, weightAmount);
        return choice.value();
    }

    private static int applyMultiplier(int baseSellPrice, double multiplier) {
        if (baseSellPrice <= 0) {
            return 0;
        }
        return Math.max(0, (int) Math.floor(baseSellPrice * multiplier));
    }
}

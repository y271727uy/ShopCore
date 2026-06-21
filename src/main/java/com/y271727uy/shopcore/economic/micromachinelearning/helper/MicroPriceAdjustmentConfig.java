package com.y271727uy.shopcore.economic.micromachinelearning.helper;

import com.y271727uy.shopcore.economic.micromachinelearning.weight.WeightConfig;

import java.util.Objects;

/**
 * Configuration for the helper facade used by external price modules.
 *
 * @param weightConfig  player-item weight table limits
 * @param useScoreModel whether candidate multipliers may be selected by the tiny score model
 */
public record MicroPriceAdjustmentConfig(
        WeightConfig weightConfig,
        boolean useScoreModel
) {
    public static final MicroPriceAdjustmentConfig DEFAULT = new MicroPriceAdjustmentConfig(
            WeightConfig.DEFAULT,
            false
    );

    public MicroPriceAdjustmentConfig {
        Objects.requireNonNull(weightConfig, "weightConfig");
    }

    public static MicroPriceAdjustmentConfig withScoreModel() {
        return new MicroPriceAdjustmentConfig(WeightConfig.DEFAULT, true);
    }
}

package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.nashequilibrium;

import java.util.List;
import java.util.Objects;

/**
 * Complete output of one market balancing pass.
 */
public record EquilibriumSnapshot(
        String marketKey,
        List<PriceAdjustment> adjustments,
        EquilibriumScore score
) {
    public EquilibriumSnapshot {
        Objects.requireNonNull(marketKey, "marketKey");
        marketKey = marketKey.trim();
        if (marketKey.isEmpty()) {
            throw new IllegalArgumentException("marketKey cannot be blank");
        }
        adjustments = List.copyOf(Objects.requireNonNull(adjustments, "adjustments"));
        Objects.requireNonNull(score, "score");
    }

    public boolean isStable(EquilibriumConfig config) {
        return score.isStable(config);
    }
}

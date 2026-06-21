package com.y271727uy.shopcore.economic.micromachinelearning.weight;

/**
 * Tunable limits for a player-scoped usage weight table.
 *
 * @param maxItemWeight     maximum weight for one item inside one player space
 * @param maxPlayers        maximum tracked player spaces
 * @param maxItemsPerPlayer maximum tracked items inside one player space
 * @param decayPerTick      weight removed per elapsed tick when an entry is touched
 * @param pressureResistance weight where pressure reaches roughly 50%
 * @param maxPenalty        maximum penalty exposed by {@link WeightSnapshot#penaltyMultiplier()}
 */
public record WeightConfig(
        double maxItemWeight,
        int maxPlayers,
        int maxItemsPerPlayer,
        double decayPerTick,
        double pressureResistance,
        double maxPenalty
) {
    public static final WeightConfig DEFAULT = new WeightConfig(
            100.0D,
            64,
            128,
            0.01D,
            40.0D,
            0.50D
    );

    public WeightConfig(
            double maxItemWeight,
            int maxItemsPerPlayer,
            double decayPerTick,
            double pressureResistance,
            double maxPenalty
    ) {
        this(maxItemWeight, 64, maxItemsPerPlayer, decayPerTick, pressureResistance, maxPenalty);
    }

    public WeightConfig {
        requirePositive(maxItemWeight, "maxItemWeight");
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("maxPlayers must be positive");
        }
        if (maxItemsPerPlayer <= 0) {
            throw new IllegalArgumentException("maxItemsPerPlayer must be positive");
        }
        requireFiniteNonNegative(decayPerTick, "decayPerTick");
        requirePositive(pressureResistance, "pressureResistance");
        requireRange(maxPenalty, "maxPenalty", 0.0D, 1.0D);
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite positive value");
        }
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
        }
    }

    private static void requireRange(double value, String name, double min, double max) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(name + " must be in [" + min + ", " + max + "]");
        }
    }
}

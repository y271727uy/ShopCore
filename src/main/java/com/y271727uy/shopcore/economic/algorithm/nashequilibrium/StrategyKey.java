package com.y271727uy.shopcore.economic.algorithm.nashequilibrium;

import java.util.Objects;

/**
 * Identifies one selectable economic route inside a market group.
 *
 * @param marketKey   shared group, such as crop, fish, ore, or selling_bin:fruit
 * @param strategyKey concrete route, such as an item id, recipe id, or shop entry id
 */
public record StrategyKey(String marketKey, String strategyKey) {
    public StrategyKey {
        marketKey = normalize(marketKey, "marketKey");
        strategyKey = normalize(strategyKey, "strategyKey");
    }

    public static StrategyKey of(String marketKey, String strategyKey) {
        return new StrategyKey(marketKey, strategyKey);
    }

    private static String normalize(String value, String name) {
        Objects.requireNonNull(value, name);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return normalized;
    }
}

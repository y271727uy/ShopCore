package com.y271727uy.shopcore.core.algorithm.shapleyvalue;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ShapleyValueResult<P>(
        Map<P, Double> shares,
        double emptyCoalitionValue,
        double grandCoalitionValue,
        ShapleyValueMethod method,
        int iterations
) {
    public ShapleyValueResult {
        Objects.requireNonNull(shares, "shares");
        Objects.requireNonNull(method, "method");
        shares = Collections.unmodifiableMap(new LinkedHashMap<>(shares));
    }

    public double shareOf(P participant) {
        return shares.getOrDefault(participant, 0.0D);
    }

    public double distributedValue() {
        double total = 0.0D;
        for (double share : shares.values()) {
            total += share;
        }
        return total;
    }

    public double distributableValue() {
        return grandCoalitionValue - emptyCoalitionValue;
    }
}

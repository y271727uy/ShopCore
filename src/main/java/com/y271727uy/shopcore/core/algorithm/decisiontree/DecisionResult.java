package com.y271727uy.shopcore.core.algorithm.decisiontree;

import java.util.Objects;
import java.util.Optional;

public record DecisionResult<R>(
        Optional<R> value,
        DecisionTrace trace
) {
    public DecisionResult {
        value = Objects.requireNonNullElse(value, Optional.empty());
        trace = Objects.requireNonNullElse(trace, DecisionTrace.empty());
    }

    public static <R> DecisionResult<R> decided(R value, DecisionTrace trace) {
        return new DecisionResult<>(Optional.of(Objects.requireNonNull(value, "value")), trace);
    }

    public static <R> DecisionResult<R> undecided(DecisionTrace trace) {
        return new DecisionResult<>(Optional.empty(), trace);
    }

    public boolean decided() {
        return value.isPresent();
    }

    public R orElse(R fallback) {
        return value.orElse(fallback);
    }

    public R orElseThrow() {
        return value.orElseThrow();
    }
}

package com.y271727uy.shopcore.core.algorithm.decisiontree;

import java.util.Objects;

public record DecisionContext<C>(
        C input,
        DecisionTrace trace
) {
    public DecisionContext {
        Objects.requireNonNull(input, "input");
        trace = Objects.requireNonNullElse(trace, DecisionTrace.empty());
    }

    public static <C> DecisionContext<C> of(C input) {
        return new DecisionContext<>(input, DecisionTrace.empty());
    }

    public DecisionContext<C> withTrace(DecisionTrace trace) {
        return new DecisionContext<>(input, trace);
    }
}

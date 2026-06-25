package com.y271727uy.shopcore.core.algorithm.decisiontree;

import java.util.Objects;

public record DecisionTraceStep(
        String nodeId,
        String outcome,
        String detail
) {
    public DecisionTraceStep {
        nodeId = normalize(nodeId, "nodeId");
        outcome = normalize(outcome, "outcome");
        detail = Objects.requireNonNullElse(detail, "").trim();
    }

    private static String normalize(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be blank");
        }
        return normalized;
    }
}

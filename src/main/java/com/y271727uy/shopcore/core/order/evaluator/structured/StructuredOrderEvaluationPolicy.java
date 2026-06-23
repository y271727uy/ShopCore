package com.y271727uy.shopcore.core.order.evaluator.structured;

public record StructuredOrderEvaluationPolicy(
        double missingRequiredPenalty,
        double missingOptionalPenalty,
        double shortagePenalty,
        double extraPenalty,
        double wrongPositionPenalty
) {
    public static final StructuredOrderEvaluationPolicy DEFAULT = new StructuredOrderEvaluationPolicy(
            20.0D,
            5.0D,
            8.0D,
            3.0D,
            4.0D
    );

    public StructuredOrderEvaluationPolicy {
        validatePenalty(missingRequiredPenalty, "missingRequiredPenalty");
        validatePenalty(missingOptionalPenalty, "missingOptionalPenalty");
        validatePenalty(shortagePenalty, "shortagePenalty");
        validatePenalty(extraPenalty, "extraPenalty");
        validatePenalty(wrongPositionPenalty, "wrongPositionPenalty");
    }

    private static void validatePenalty(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D) {
            throw new IllegalArgumentException(name + " must be a finite non-negative value");
        }
    }
}

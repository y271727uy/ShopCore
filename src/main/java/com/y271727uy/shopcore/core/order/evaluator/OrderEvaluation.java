package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.order.request.OrderRequestKind;

import java.util.Objects;

public record OrderEvaluation(
        OrderRequestKind requestKind,
        boolean accepted,
        double score,
        double payoutMultiplier,
        double reputationMultiplier,
        int matchedCount,
        int requestedCount,
        String reason
) {
    public static final String REASON_ACCEPTED = "accepted";
    public static final String REASON_PARTIAL = "partial";
    public static final String REASON_NO_MATCH = "no_match";
    public static final String REASON_INPUT_EMPTY = "input_empty";
    public static final String REASON_UNSUPPORTED_STRUCTURED = "unsupported_structured";

    public OrderEvaluation {
        Objects.requireNonNull(requestKind, "requestKind");
        if (!Double.isFinite(score) || score < 0.0D || score > 100.0D) {
            throw new IllegalArgumentException("score must be in [0, 100]");
        }
        if (!Double.isFinite(payoutMultiplier) || payoutMultiplier < 0.0D) {
            throw new IllegalArgumentException("payoutMultiplier must be a finite non-negative value");
        }
        if (!Double.isFinite(reputationMultiplier) || reputationMultiplier < 0.0D) {
            throw new IllegalArgumentException("reputationMultiplier must be a finite non-negative value");
        }
        if (matchedCount < 0) {
            throw new IllegalArgumentException("matchedCount cannot be negative");
        }
        if (requestedCount < 1) {
            throw new IllegalArgumentException("requestedCount must be at least 1");
        }
        reason = Objects.requireNonNull(reason, "reason").trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
    }

    public static OrderEvaluation of(
            OrderRequestKind requestKind,
            int matchedCount,
            int requestedCount,
            double minimumAcceptedScore,
            String reason
    ) {
        double score = requestedCount <= 0 ? 0.0D : Math.min(100.0D, (matchedCount * 100.0D) / requestedCount);
        boolean accepted = score >= minimumAcceptedScore;
        double multiplier = accepted ? score / 100.0D : 0.0D;
        return new OrderEvaluation(requestKind, accepted, score, multiplier, multiplier, matchedCount, requestedCount, reason);
    }
}

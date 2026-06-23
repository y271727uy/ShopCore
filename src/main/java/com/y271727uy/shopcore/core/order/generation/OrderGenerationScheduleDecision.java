package com.y271727uy.shopcore.core.order.generation;

import java.util.Objects;

public record OrderGenerationScheduleDecision(
        boolean shouldAttempt,
        String reason
) {
    public static final String REASON_ALLOWED = "allowed";
    public static final String REASON_DISABLED = "disabled";
    public static final String REASON_INTERVAL_WAIT = "interval_wait";
    public static final String REASON_RANDOM_CHANCE_FAILED = "random_chance_failed";

    public OrderGenerationScheduleDecision {
        reason = Objects.requireNonNull(reason, "reason").trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
    }

    public static OrderGenerationScheduleDecision allowed() {
        return new OrderGenerationScheduleDecision(true, REASON_ALLOWED);
    }

    public static OrderGenerationScheduleDecision denied(String reason) {
        return new OrderGenerationScheduleDecision(false, reason);
    }
}

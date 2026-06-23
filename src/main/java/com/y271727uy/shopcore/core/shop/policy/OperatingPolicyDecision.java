package com.y271727uy.shopcore.core.shop.policy;

import java.util.Objects;

public record OperatingPolicyDecision(
        OperatingPolicyKey policyKey,
        boolean shouldOpen,
        String reason
) {
    public static final String REASON_MANUAL_OPEN = "manual_open";
    public static final String REASON_MANUAL_CLOSED = "manual_closed";
    public static final String REASON_DAY_OPEN = "day_open";
    public static final String REASON_DAY_CLOSED = "day_closed";
    public static final String REASON_NIGHT_OPEN = "night_open";
    public static final String REASON_NIGHT_CLOSED = "night_closed";
    public static final String REASON_ALWAYS_OPEN = "always_open";
    public static final String REASON_UNKNOWN_POLICY = "unknown_policy";

    public OperatingPolicyDecision {
        Objects.requireNonNull(policyKey, "policyKey");
        reason = Objects.requireNonNull(reason, "reason").trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
    }

    public static OperatingPolicyDecision open(OperatingPolicyKey policyKey, String reason) {
        return new OperatingPolicyDecision(policyKey, true, reason);
    }

    public static OperatingPolicyDecision closed(OperatingPolicyKey policyKey, String reason) {
        return new OperatingPolicyDecision(policyKey, false, reason);
    }
}

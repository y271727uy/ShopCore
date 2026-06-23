package com.y271727uy.shopcore.core.shop.policy;

import java.util.Objects;

public final class OperatingPolicyEvaluator {
    private OperatingPolicyEvaluator() {
    }

    public static OperatingPolicyDecision evaluate(OperatingPolicyContext context) {
        Objects.requireNonNull(context, "context");
        OperatingPolicyKey policyKey = context.policyKey();

        if (OperatingPolicyKey.MANUAL.equals(policyKey)) {
            return context.manualOpen()
                    ? OperatingPolicyDecision.open(policyKey, OperatingPolicyDecision.REASON_MANUAL_OPEN)
                    : OperatingPolicyDecision.closed(policyKey, OperatingPolicyDecision.REASON_MANUAL_CLOSED);
        }
        if (OperatingPolicyKey.DAY_OPEN.equals(policyKey)) {
            return context.isDay()
                    ? OperatingPolicyDecision.open(policyKey, OperatingPolicyDecision.REASON_DAY_OPEN)
                    : OperatingPolicyDecision.closed(policyKey, OperatingPolicyDecision.REASON_DAY_CLOSED);
        }
        if (OperatingPolicyKey.NIGHT_OPEN.equals(policyKey)) {
            return context.isNight()
                    ? OperatingPolicyDecision.open(policyKey, OperatingPolicyDecision.REASON_NIGHT_OPEN)
                    : OperatingPolicyDecision.closed(policyKey, OperatingPolicyDecision.REASON_NIGHT_CLOSED);
        }
        if (OperatingPolicyKey.ALWAYS_OPEN.equals(policyKey)) {
            return OperatingPolicyDecision.open(policyKey, OperatingPolicyDecision.REASON_ALWAYS_OPEN);
        }

        return OperatingPolicyDecision.closed(policyKey, OperatingPolicyDecision.REASON_UNKNOWN_POLICY);
    }
}

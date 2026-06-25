package com.y271727uy.shopcore.core.algorithm.decisiontree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record DecisionTrace(List<DecisionTraceStep> steps) {
    public DecisionTrace {
        steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
    }

    public static DecisionTrace empty() {
        return new DecisionTrace(List.of());
    }

    public DecisionTrace append(String nodeId, String outcome) {
        return append(nodeId, outcome, "");
    }

    public DecisionTrace append(String nodeId, String outcome, String detail) {
        List<DecisionTraceStep> updated = new ArrayList<>(steps.size() + 1);
        updated.addAll(steps);
        updated.add(new DecisionTraceStep(nodeId, outcome, detail));
        return new DecisionTrace(updated);
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public String describe() {
        if (steps.isEmpty()) {
            return "";
        }

        List<String> parts = new ArrayList<>(steps.size());
        for (DecisionTraceStep step : steps) {
            String text = step.nodeId() + "=" + step.outcome();
            if (!step.detail().isBlank()) {
                text += "(" + step.detail() + ")";
            }
            parts.add(text);
        }
        return String.join(" -> ", parts);
    }
}

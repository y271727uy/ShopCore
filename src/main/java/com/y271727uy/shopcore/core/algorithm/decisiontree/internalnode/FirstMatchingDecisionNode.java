package com.y271727uy.shopcore.core.algorithm.decisiontree.internalnode;

import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionContext;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionNode;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionResult;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionTrace;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class FirstMatchingDecisionNode<C, R> implements DecisionNode<C, R> {
    private final String nodeId;
    private final List<Case<C, R>> cases;
    private final Optional<DecisionNode<C, R>> fallbackNode;

    public FirstMatchingDecisionNode(String nodeId, List<Case<C, R>> cases) {
        this(nodeId, cases, null);
    }

    public FirstMatchingDecisionNode(String nodeId, List<Case<C, R>> cases, DecisionNode<C, R> fallbackNode) {
        this.nodeId = normalizeNodeId(nodeId);
        this.cases = List.copyOf(Objects.requireNonNull(cases, "cases"));
        this.fallbackNode = Optional.ofNullable(fallbackNode);
    }

    @Override
    public String nodeId() {
        return nodeId;
    }

    @Override
    public DecisionResult<R> evaluate(DecisionContext<C> context) {
        Objects.requireNonNull(context, "context");
        for (Case<C, R> currentCase : cases) {
            if (!currentCase.condition().test(context.input())) {
                continue;
            }
            DecisionTrace trace = context.trace().append(nodeId, "matched", currentCase.caseId());
            return currentCase.node().evaluate(context.withTrace(trace));
        }

        DecisionTrace trace = context.trace().append(nodeId, "fallback");
        return fallbackNode
                .map(node -> node.evaluate(context.withTrace(trace)))
                .orElseGet(() -> DecisionResult.undecided(trace));
    }

    private static String normalizeNodeId(String nodeId) {
        String normalized = Objects.requireNonNull(nodeId, "nodeId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("nodeId cannot be blank");
        }
        return normalized;
    }

    public record Case<C, R>(
            String caseId,
            Predicate<C> condition,
            DecisionNode<C, R> node
    ) {
        public Case {
            caseId = normalizeCaseId(caseId);
            Objects.requireNonNull(condition, "condition");
            Objects.requireNonNull(node, "node");
        }

        public static <C, R> Case<C, R> of(String caseId, Predicate<C> condition, DecisionNode<C, R> node) {
            return new Case<>(caseId, condition, node);
        }

        private static String normalizeCaseId(String caseId) {
            String normalized = Objects.requireNonNull(caseId, "caseId").trim();
            if (normalized.isEmpty()) {
                throw new IllegalArgumentException("caseId cannot be blank");
            }
            return normalized;
        }
    }
}

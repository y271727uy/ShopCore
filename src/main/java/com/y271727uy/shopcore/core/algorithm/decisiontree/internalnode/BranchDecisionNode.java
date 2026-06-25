package com.y271727uy.shopcore.core.algorithm.decisiontree.internalnode;

import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionContext;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionNode;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionResult;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionTrace;

import java.util.Objects;
import java.util.function.Predicate;

public class BranchDecisionNode<C, R> implements DecisionNode<C, R> {
    private final String nodeId;
    private final Predicate<C> condition;
    private final DecisionNode<C, R> trueNode;
    private final DecisionNode<C, R> falseNode;
    private final String trueDetail;
    private final String falseDetail;

    public BranchDecisionNode(
            String nodeId,
            Predicate<C> condition,
            DecisionNode<C, R> trueNode,
            DecisionNode<C, R> falseNode
    ) {
        this(nodeId, condition, trueNode, falseNode, "true", "false");
    }

    public BranchDecisionNode(
            String nodeId,
            Predicate<C> condition,
            DecisionNode<C, R> trueNode,
            DecisionNode<C, R> falseNode,
            String trueDetail,
            String falseDetail
    ) {
        this.nodeId = normalizeNodeId(nodeId);
        this.condition = Objects.requireNonNull(condition, "condition");
        this.trueNode = Objects.requireNonNull(trueNode, "trueNode");
        this.falseNode = Objects.requireNonNull(falseNode, "falseNode");
        this.trueDetail = Objects.requireNonNullElse(trueDetail, "").trim();
        this.falseDetail = Objects.requireNonNullElse(falseDetail, "").trim();
    }

    @Override
    public String nodeId() {
        return nodeId;
    }

    @Override
    public DecisionResult<R> evaluate(DecisionContext<C> context) {
        Objects.requireNonNull(context, "context");
        boolean matched = condition.test(context.input());
        DecisionTrace trace = context.trace().append(nodeId, matched ? "true" : "false", matched ? trueDetail : falseDetail);
        DecisionNode<C, R> nextNode = matched ? trueNode : falseNode;
        return nextNode.evaluate(context.withTrace(trace));
    }

    private static String normalizeNodeId(String nodeId) {
        String normalized = Objects.requireNonNull(nodeId, "nodeId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("nodeId cannot be blank");
        }
        return normalized;
    }
}

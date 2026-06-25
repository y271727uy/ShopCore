package com.y271727uy.shopcore.core.algorithm.decisiontree.leafnode;

import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionContext;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionNode;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionResult;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionTrace;

import java.util.Objects;
import java.util.function.Function;

public class ValueDecisionLeaf<C, R> implements DecisionNode<C, R> {
    private final String nodeId;
    private final Function<C, R> valueFactory;
    private final String detail;

    public ValueDecisionLeaf(String nodeId, R value) {
        this(nodeId, ignored -> value, "");
    }

    public ValueDecisionLeaf(String nodeId, R value, String detail) {
        this(nodeId, ignored -> value, detail);
    }

    public ValueDecisionLeaf(String nodeId, Function<C, R> valueFactory) {
        this(nodeId, valueFactory, "");
    }

    public ValueDecisionLeaf(String nodeId, Function<C, R> valueFactory, String detail) {
        this.nodeId = normalizeNodeId(nodeId);
        this.valueFactory = Objects.requireNonNull(valueFactory, "valueFactory");
        this.detail = Objects.requireNonNullElse(detail, "").trim();
    }

    @Override
    public String nodeId() {
        return nodeId;
    }

    @Override
    public DecisionResult<R> evaluate(DecisionContext<C> context) {
        Objects.requireNonNull(context, "context");
        DecisionTrace trace = context.trace().append(nodeId, "decided", detail);
        return DecisionResult.decided(valueFactory.apply(context.input()), trace);
    }

    private static String normalizeNodeId(String nodeId) {
        String normalized = Objects.requireNonNull(nodeId, "nodeId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("nodeId cannot be blank");
        }
        return normalized;
    }
}

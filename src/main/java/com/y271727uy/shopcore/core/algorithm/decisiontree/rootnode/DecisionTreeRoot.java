package com.y271727uy.shopcore.core.algorithm.decisiontree.rootnode;

import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionContext;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionNode;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionResult;
import com.y271727uy.shopcore.core.algorithm.decisiontree.DecisionTrace;

import java.util.Objects;

public class DecisionTreeRoot<C, R> implements DecisionNode<C, R> {
    private final String treeId;
    private final DecisionNode<C, R> rootNode;

    public DecisionTreeRoot(String treeId, DecisionNode<C, R> rootNode) {
        this.treeId = normalizeTreeId(treeId);
        this.rootNode = Objects.requireNonNull(rootNode, "rootNode");
    }

    @Override
    public String nodeId() {
        return treeId;
    }

    @Override
    public DecisionResult<R> evaluate(DecisionContext<C> context) {
        Objects.requireNonNull(context, "context");
        DecisionTrace trace = context.trace().append(treeId, "root");
        return rootNode.evaluate(context.withTrace(trace));
    }

    private static String normalizeTreeId(String treeId) {
        String normalized = Objects.requireNonNull(treeId, "treeId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("treeId cannot be blank");
        }
        return normalized;
    }
}

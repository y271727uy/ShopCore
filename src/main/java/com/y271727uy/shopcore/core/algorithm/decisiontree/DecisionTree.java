package com.y271727uy.shopcore.core.algorithm.decisiontree;

import com.y271727uy.shopcore.core.algorithm.decisiontree.rootnode.DecisionTreeRoot;

import java.util.Objects;

public record DecisionTree<C, R>(DecisionTreeRoot<C, R> root) {
    public DecisionTree {
        Objects.requireNonNull(root, "root");
    }

    public static <C, R> DecisionTree<C, R> of(String treeId, DecisionNode<C, R> rootNode) {
        return new DecisionTree<>(new DecisionTreeRoot<>(treeId, rootNode));
    }

    public DecisionResult<R> evaluate(C input) {
        return root.evaluate(input);
    }

    public DecisionResult<R> evaluate(DecisionContext<C> context) {
        return root.evaluate(context);
    }
}

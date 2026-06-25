package com.y271727uy.shopcore.core.algorithm.decisiontree;

public interface DecisionNode<C, R> {
    String nodeId();

    DecisionResult<R> evaluate(DecisionContext<C> context);

    default DecisionResult<R> evaluate(C input) {
        return evaluate(DecisionContext.of(input));
    }
}

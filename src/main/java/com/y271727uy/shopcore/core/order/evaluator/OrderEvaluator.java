package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.order.request.OrderRequestKind;

public interface OrderEvaluator {
    boolean supports(OrderRequestKind kind);

    OrderEvaluation evaluate(OrderEvaluationContext context);
}

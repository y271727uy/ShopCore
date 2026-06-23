package com.y271727uy.shopcore.core.order.evaluator.structured;

import com.y271727uy.shopcore.core.order.request.StructuredItemOrderRequest;

import java.util.List;
import java.util.Objects;

public record StructuredOrderEvaluationContext(
        StructuredItemOrderRequest request,
        List<StructuredDeliveryPart> deliveredParts,
        double minimumAcceptedScore
) {
    public StructuredOrderEvaluationContext {
        Objects.requireNonNull(request, "request");
        deliveredParts = List.copyOf(Objects.requireNonNull(deliveredParts, "deliveredParts"));
        if (!Double.isFinite(minimumAcceptedScore) || minimumAcceptedScore < 0.0D || minimumAcceptedScore > 100.0D) {
            throw new IllegalArgumentException("minimumAcceptedScore must be in [0, 100]");
        }
    }
}

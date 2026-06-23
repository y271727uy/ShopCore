package com.y271727uy.shopcore.core.order.evaluator.structured;

import com.y271727uy.shopcore.core.order.evaluator.OrderEvaluation;
import com.y271727uy.shopcore.core.order.request.OrderRequestKind;
import com.y271727uy.shopcore.core.order.request.StructuredOrderPart;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StructuredOrderEvaluator {
    private final StructuredOrderEvaluationPolicy policy;

    public StructuredOrderEvaluator() {
        this(StructuredOrderEvaluationPolicy.DEFAULT);
    }

    public StructuredOrderEvaluator(StructuredOrderEvaluationPolicy policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public OrderEvaluation evaluate(StructuredOrderEvaluationContext context) {
        Objects.requireNonNull(context, "context");

        Map<PartKey, Integer> delivered = new HashMap<>();
        Map<net.minecraft.resources.ResourceLocation, Integer> deliveredByPart = new HashMap<>();
        for (StructuredDeliveryPart part : context.deliveredParts()) {
            delivered.merge(new PartKey(part.partKey(), part.position()), part.count(), Integer::sum);
            deliveredByPart.merge(part.partKey(), part.count(), Integer::sum);
        }

        int requestedCount = context.request().requestedCount();
        int matchedCount = 0;
        double penalty = 0.0D;

        for (StructuredOrderPart part : context.request().parts()) {
            int matchedAtPosition = delivered.getOrDefault(new PartKey(part.partKey(), part.position()), 0);
            int matchedAnyPosition = deliveredByPart.getOrDefault(part.partKey(), 0);
            int matched = part.position() == StructuredOrderPart.ANY_POSITION
                    ? matchedAnyPosition
                    : matchedAtPosition;

            matchedCount += Math.min(part.requestedCount(), matched);
            if (matched == 0) {
                penalty += part.required() ? policy.missingRequiredPenalty() : policy.missingOptionalPenalty();
                continue;
            }
            if (matched < part.requestedCount()) {
                penalty += (part.requestedCount() - matched) * policy.shortagePenalty();
            }
            if (part.position() != StructuredOrderPart.ANY_POSITION && matchedAtPosition == 0 && matchedAnyPosition > 0) {
                penalty += policy.wrongPositionPenalty();
            }
        }

        int extraCount = Math.max(0, context.deliveredParts().stream().mapToInt(StructuredDeliveryPart::count).sum() - requestedCount);
        penalty += extraCount * policy.extraPenalty();
        double rawScore = requestedCount <= 0 ? 0.0D : (matchedCount * 100.0D) / requestedCount;
        double score = Math.max(0.0D, Math.min(100.0D, rawScore - penalty));
        boolean accepted = score >= context.minimumAcceptedScore();
        double multiplier = accepted ? score / 100.0D : 0.0D;
        return new OrderEvaluation(
                OrderRequestKind.STRUCTURED_ITEM,
                accepted,
                score,
                multiplier,
                multiplier,
                matchedCount,
                requestedCount,
                reason(accepted, matchedCount)
        );
    }

    private static String reason(boolean accepted, int matchedCount) {
        if (accepted) {
            return OrderEvaluation.REASON_ACCEPTED;
        }
        if (matchedCount <= 0) {
            return OrderEvaluation.REASON_NO_MATCH;
        }
        return OrderEvaluation.REASON_PARTIAL;
    }

    private record PartKey(net.minecraft.resources.ResourceLocation partKey, int position) {
    }
}

package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.order.request.OrderRequest;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record OrderEvaluationContext(
        OrderRequest request,
        ItemStack deliveredStack,
        double minimumAcceptedScore
) {
    public static final double DEFAULT_MINIMUM_ACCEPTED_SCORE = 100.0D;

    public OrderEvaluationContext {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(deliveredStack, "deliveredStack");
        deliveredStack = deliveredStack.copy();
        if (!Double.isFinite(minimumAcceptedScore) || minimumAcceptedScore < 0.0D || minimumAcceptedScore > 100.0D) {
            throw new IllegalArgumentException("minimumAcceptedScore must be in [0, 100]");
        }
    }

    public static OrderEvaluationContext exact(OrderRequest request, ItemStack deliveredStack) {
        return new OrderEvaluationContext(request, deliveredStack, DEFAULT_MINIMUM_ACCEPTED_SCORE);
    }
}

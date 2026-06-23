package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.request.ItemListOrderRequest;
import com.y271727uy.shopcore.core.order.request.OrderRequest;
import com.y271727uy.shopcore.core.order.request.OrderRequestKind;
import com.y271727uy.shopcore.core.order.request.SingleItemOrderRequest;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class DefaultOrderEvaluator implements OrderEvaluator {
    public static final DefaultOrderEvaluator INSTANCE = new DefaultOrderEvaluator();

    private DefaultOrderEvaluator() {
    }

    @Override
    public boolean supports(OrderRequestKind kind) {
        return kind == OrderRequestKind.SINGLE_ITEM
                || kind == OrderRequestKind.ITEM_LIST
                || kind == OrderRequestKind.STRUCTURED_ITEM;
    }

    @Override
    public OrderEvaluation evaluate(OrderEvaluationContext context) {
        Objects.requireNonNull(context, "context");
        OrderRequest request = context.request();
        ItemStack deliveredStack = context.deliveredStack();
        if (deliveredStack.isEmpty()) {
            return OrderEvaluation.of(
                    request.kind(),
                    0,
                    request.requestedCount(),
                    context.minimumAcceptedScore(),
                    OrderEvaluation.REASON_INPUT_EMPTY
            );
        }

        return switch (request.kind()) {
            case SINGLE_ITEM -> evaluateSingle((SingleItemOrderRequest) request, deliveredStack, context.minimumAcceptedScore());
            case ITEM_LIST -> evaluateList((ItemListOrderRequest) request, deliveredStack, context.minimumAcceptedScore());
            case STRUCTURED_ITEM -> OrderEvaluation.of(
                    request.kind(),
                    0,
                    request.requestedCount(),
                    context.minimumAcceptedScore(),
                    OrderEvaluation.REASON_UNSUPPORTED_STRUCTURED
            );
        };
    }

    private static OrderEvaluation evaluateSingle(SingleItemOrderRequest request, ItemStack deliveredStack, double minimumAcceptedScore) {
        int matched = ItemStack.isSameItemSameTags(deliveredStack, request.requestedItem())
                ? Math.min(deliveredStack.getCount(), request.requestedCount())
                : 0;
        return OrderEvaluation.of(
                request.kind(),
                matched,
                request.requestedCount(),
                minimumAcceptedScore,
                reason(matched, request.requestedCount())
        );
    }

    private static OrderEvaluation evaluateList(ItemListOrderRequest request, ItemStack deliveredStack, double minimumAcceptedScore) {
        int matched = 0;
        for (OrderLine line : request.lines()) {
            if (line.matches(deliveredStack)) {
                matched += Math.min(deliveredStack.getCount(), line.remainingCount());
            }
        }
        return OrderEvaluation.of(
                request.kind(),
                matched,
                request.requestedCount(),
                minimumAcceptedScore,
                reason(matched, request.requestedCount())
        );
    }

    private static String reason(int matched, int requested) {
        if (matched <= 0) {
            return OrderEvaluation.REASON_NO_MATCH;
        }
        if (matched >= requested) {
            return OrderEvaluation.REASON_ACCEPTED;
        }
        return OrderEvaluation.REASON_PARTIAL;
    }
}

package com.y271727uy.shopcore.core.order.request;

/**
 * The customer-facing requirement for an order.
 * Different request kinds intentionally do not share one delivery algorithm.
 */
public interface OrderRequest {
    OrderRequestKind kind();

    int requestedCount();

    long baseValue();

    default boolean requiresStructuredEvaluation() {
        return kind() == OrderRequestKind.STRUCTURED_ITEM;
    }
}

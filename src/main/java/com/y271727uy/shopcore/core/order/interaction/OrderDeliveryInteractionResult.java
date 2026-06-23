package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.delivery.OrderDeliveryResult;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementResult;

import java.util.Objects;
import java.util.Optional;

public record OrderDeliveryInteractionResult(
        OrderInteractionStatus status,
        ShopOrder order,
        OrderDeliveryResult deliveryResult,
        Optional<OrderSettlementResult> settlementResult
) {
    public OrderDeliveryInteractionResult {
        Objects.requireNonNull(status, "status");
        settlementResult = Objects.requireNonNullElse(settlementResult, Optional.empty());
    }

    public static OrderDeliveryInteractionResult failed(OrderInteractionStatus status) {
        if (status == OrderInteractionStatus.DELIVERED || status == OrderInteractionStatus.COMPLETED) {
            throw new IllegalArgumentException("use a successful factory for " + status);
        }
        return new OrderDeliveryInteractionResult(status, null, null, Optional.empty());
    }

    public static OrderDeliveryInteractionResult delivered(ShopOrder order, OrderDeliveryResult deliveryResult) {
        return new OrderDeliveryInteractionResult(
                OrderInteractionStatus.DELIVERED,
                Objects.requireNonNull(order, "order"),
                Objects.requireNonNull(deliveryResult, "deliveryResult"),
                Optional.empty()
        );
    }

    public static OrderDeliveryInteractionResult completed(
            ShopOrder order,
            OrderDeliveryResult deliveryResult,
            OrderSettlementResult settlementResult
    ) {
        return new OrderDeliveryInteractionResult(
                OrderInteractionStatus.COMPLETED,
                Objects.requireNonNull(order, "order"),
                Objects.requireNonNull(deliveryResult, "deliveryResult"),
                Optional.of(Objects.requireNonNull(settlementResult, "settlementResult"))
        );
    }
}

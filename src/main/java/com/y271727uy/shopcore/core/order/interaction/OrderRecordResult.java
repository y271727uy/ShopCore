package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.order.ShopOrder;

import java.util.Objects;

public record OrderRecordResult(
        OrderInteractionStatus status,
        ShopOrder order
) {
    public OrderRecordResult {
        Objects.requireNonNull(status, "status");
    }

    public static OrderRecordResult recorded(ShopOrder order) {
        return new OrderRecordResult(OrderInteractionStatus.RECORDED, Objects.requireNonNull(order, "order"));
    }

    public static OrderRecordResult failed(OrderInteractionStatus status) {
        if (status == OrderInteractionStatus.RECORDED || status == OrderInteractionStatus.COMPLETED) {
            throw new IllegalArgumentException("use a successful factory for " + status);
        }
        return new OrderRecordResult(status, null);
    }
}

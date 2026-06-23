package com.y271727uy.shopcore.core.order.settlement;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.evaluator.OrderEvaluation;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;

import java.util.Objects;

public record OrderSettlementBridgeContext(
        ShopInstance shop,
        ShopOrder order,
        OrderEvaluation evaluation,
        OrderSettlementBinding binding,
        double reputationBase
) {
    public OrderSettlementBridgeContext {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(evaluation, "evaluation");
        Objects.requireNonNull(binding, "binding");
        if (!shop.shopId().equals(order.shopId())) {
            throw new IllegalArgumentException("order belongs to another shop");
        }
        if (!Double.isFinite(reputationBase) || reputationBase < 0.0D) {
            throw new IllegalArgumentException("reputationBase must be a finite non-negative value");
        }
    }
}

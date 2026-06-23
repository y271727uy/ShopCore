package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.request.OrderRequest;

import java.util.Objects;

public record CustomerOrderDecision(
        CustomerProfile customerProfile,
        ShopOrder order,
        OrderRequest request
) {
    public CustomerOrderDecision {
        Objects.requireNonNull(customerProfile, "customerProfile");
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(request, "request");
    }
}

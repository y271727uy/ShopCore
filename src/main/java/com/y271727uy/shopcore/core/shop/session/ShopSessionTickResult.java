package com.y271727uy.shopcore.core.shop.session;

import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleResult;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyDecision;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ShopSessionTickResult(
        ShopInstance shop,
        ShopOrderBook orderBook,
        OperatingPolicyDecision operatingDecision,
        Optional<ShopOpeningResult> openingResult,
        List<OrderLifecycleResult> orderEvents
) {
    public ShopSessionTickResult {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(orderBook, "orderBook");
        Objects.requireNonNull(operatingDecision, "operatingDecision");
        openingResult = Objects.requireNonNullElse(openingResult, Optional.empty());
        orderEvents = List.copyOf(Objects.requireNonNull(orderEvents, "orderEvents"));
    }

    public boolean open() {
        return shop.isOpen();
    }
}

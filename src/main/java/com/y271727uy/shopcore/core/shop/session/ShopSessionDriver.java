package com.y271727uy.shopcore.core.shop.session;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleResult;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleService;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningContext;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningValidator;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyDecision;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ShopSessionDriver {
    private ShopSessionDriver() {
    }

    public static ShopSessionTickResult tick(ShopSessionTickContext context) {
        OperatingPolicyDecision operatingDecision = OperatingPolicyEvaluator.evaluate(context.operatingPolicyContext());
        Optional<ShopOpeningResult> openingResult = context.openingRuleSet()
                .map(ruleSet -> ShopOpeningValidator.validate(
                        new ShopOpeningContext(context.shop(), context.listings(), context.openingAttributes()),
                        ruleSet
                ));

        boolean shouldOpen = operatingDecision.shouldOpen()
                && openingResult.map(ShopOpeningResult::allowedToOpen).orElse(true);

        ShopInstance shop = transitionShop(context.shop(), shouldOpen, context.gameTime());
        ShopOrderBook orderBook = context.orderBook();
        List<OrderLifecycleResult> events = new ArrayList<>();

        for (ShopOrder order : orderBook.orders()) {
            OrderLifecycleResult result = OrderLifecycleService.refresh(order, context.gameTime());
            if (result.countsAsExpired()) {
                shop = shop.withCurrentSession(shop.currentSession().recordOrderExpired());
            }
            if (result.afterOrder() != null) {
                orderBook = orderBook.replace(result.afterOrder());
            }
            if (result.status() != com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleStatus.UNCHANGED) {
                events.add(result);
            }
        }

        if (context.incomingOrder().isPresent()) {
            OrderLifecycleResult createResult = OrderLifecycleService.tryCreate(shop, orderBook.activeOrders(), context.incomingOrder().get());
            events.add(createResult);
            if (createResult.countsAsCreated() && createResult.afterOrder() != null) {
                orderBook = orderBook.add(createResult.afterOrder());
                shop = shop.withCurrentSession(shop.currentSession().recordOrderCreated());
            }
        }

        return new ShopSessionTickResult(shop, orderBook, operatingDecision, openingResult, events);
    }

    private static ShopInstance transitionShop(ShopInstance shop, boolean shouldOpen, long gameTime) {
        if (shouldOpen && !shop.isOpen()) {
            return shop.open(gameTime);
        }
        if (!shouldOpen && shop.isOpen()) {
            return shop.close(gameTime);
        }
        return shop;
    }
}

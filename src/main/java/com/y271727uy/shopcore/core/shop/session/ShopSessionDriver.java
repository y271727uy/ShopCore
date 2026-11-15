package com.y271727uy.shopcore.core.shop.session;

import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.order.lifecycle.OrderBookLifecycleService;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningContext;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningValidator;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyDecision;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyEvaluator;

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
        var lifecycle = OrderBookLifecycleService.refresh(shop, context.orderBook(),
                context.gameTime(), context.incomingOrder());
        return new ShopSessionTickResult(lifecycle.shop(), lifecycle.orderBook(), operatingDecision,
                openingResult, lifecycle.events());
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

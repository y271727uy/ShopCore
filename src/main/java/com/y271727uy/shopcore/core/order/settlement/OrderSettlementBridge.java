package com.y271727uy.shopcore.core.order.settlement;

import java.util.Objects;

public final class OrderSettlementBridge {
    private OrderSettlementBridge() {
    }

    public static OrderSettlementResult settle(OrderSettlementBridgeContext context) {
        Objects.requireNonNull(context, "context");
        OrderSettlementContext settlementContext = switch (context.binding().mode()) {
            case CURRENCY_ITEMS -> OrderSettlementContext.currencyItems(
                    context.order(),
                    context.evaluation(),
                    true,
                    context.reputationBase()
            );
            case ACCOUNT_DEPOSIT -> OrderSettlementContext.accountDeposit(
                    context.order(),
                    context.evaluation(),
                    context.binding().accountUuid().orElseThrow(),
                    context.binding().taxExempt(),
                    context.reputationBase()
            );
            case CALCULATE_ONLY -> OrderSettlementContext.calculateOnly(
                    context.order(),
                    context.evaluation(),
                    context.binding().taxExempt(),
                    context.reputationBase()
            );
        };
        return OrderSettlementService.settle(settlementContext, context.shop().currentSession());
    }

    public static OrderSettlementResult settle(
            OrderSettlementBridgeContext context,
            OrderSettlementBindingProvider bindingProvider
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(bindingProvider, "bindingProvider");
        return settle(new OrderSettlementBridgeContext(
                context.shop(),
                context.order(),
                context.evaluation(),
                bindingProvider.bindingFor(context.shop()),
                context.reputationBase()
        ));
    }
}

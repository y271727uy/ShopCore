package com.y271727uy.shopcore.core.order.settlement;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.evaluator.OrderEvaluation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record OrderSettlementContext(
        ShopOrder order,
        OrderEvaluation evaluation,
        OrderSettlementMode mode,
        Optional<UUID> accountUuid,
        boolean taxExempt,
        double reputationBase
) {
    public OrderSettlementContext {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(evaluation, "evaluation");
        Objects.requireNonNull(mode, "mode");
        accountUuid = Objects.requireNonNullElse(accountUuid, Optional.empty());
        if (mode == OrderSettlementMode.ACCOUNT_DEPOSIT && accountUuid.isEmpty()) {
            throw new IllegalArgumentException("ACCOUNT_DEPOSIT requires accountUuid");
        }
        if (!Double.isFinite(reputationBase) || reputationBase < 0.0D) {
            throw new IllegalArgumentException("reputationBase must be a finite non-negative value");
        }
    }

    public static OrderSettlementContext currencyItems(ShopOrder order, OrderEvaluation evaluation, boolean taxExempt, double reputationBase) {
        return new OrderSettlementContext(order, evaluation, OrderSettlementMode.CURRENCY_ITEMS, Optional.empty(), taxExempt, reputationBase);
    }

    public static OrderSettlementContext accountDeposit(ShopOrder order, OrderEvaluation evaluation, UUID accountUuid, boolean taxExempt, double reputationBase) {
        return new OrderSettlementContext(order, evaluation, OrderSettlementMode.ACCOUNT_DEPOSIT, Optional.of(accountUuid), taxExempt, reputationBase);
    }

    public static OrderSettlementContext calculateOnly(ShopOrder order, OrderEvaluation evaluation, boolean taxExempt, double reputationBase) {
        return new OrderSettlementContext(order, evaluation, OrderSettlementMode.CALCULATE_ONLY, Optional.empty(), taxExempt, reputationBase);
    }
}

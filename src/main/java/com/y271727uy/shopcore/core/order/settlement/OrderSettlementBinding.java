package com.y271727uy.shopcore.core.order.settlement;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record OrderSettlementBinding(
        OrderSettlementMode mode,
        Optional<UUID> accountUuid,
        boolean taxExempt
) {
    public static final OrderSettlementBinding CURRENCY_ITEMS = new OrderSettlementBinding(
            OrderSettlementMode.CURRENCY_ITEMS,
            Optional.empty(),
            true
    );

    public OrderSettlementBinding {
        Objects.requireNonNull(mode, "mode");
        accountUuid = Objects.requireNonNullElse(accountUuid, Optional.empty());
        if (mode == OrderSettlementMode.ACCOUNT_DEPOSIT && accountUuid.isEmpty()) {
            throw new IllegalArgumentException("ACCOUNT_DEPOSIT requires accountUuid");
        }
    }

    public static OrderSettlementBinding currencyItems() {
        return CURRENCY_ITEMS;
    }

    public static OrderSettlementBinding accountDeposit(UUID accountUuid, boolean taxExempt) {
        return new OrderSettlementBinding(OrderSettlementMode.ACCOUNT_DEPOSIT, Optional.of(accountUuid), taxExempt);
    }

    public static OrderSettlementBinding calculateOnly(boolean taxExempt) {
        return new OrderSettlementBinding(OrderSettlementMode.CALCULATE_ONLY, Optional.empty(), taxExempt);
    }
}

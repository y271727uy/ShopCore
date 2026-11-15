package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;

import java.util.Objects;

/** Completion revenue/counters belong exclusively to successful settlement, never status polling. */
public final class OrderLifecycleAccounting {
    private OrderLifecycleAccounting() { }

    public static BusinessSessionStats record(BusinessSessionStats stats, OrderLifecycleStatus event) {
        Objects.requireNonNull(stats, "stats");
        return switch (Objects.requireNonNull(event, "event")) {
            case CREATED -> stats.recordOrderCreated();
            case EXPIRED -> stats.recordOrderExpired();
            case CANCELLED -> stats.recordOrderCancelled();
            default -> stats;
        };
    }
}

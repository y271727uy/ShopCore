package com.y271727uy.shopcore.core.order.lifecycle;

public enum OrderLifecycleStatus {
    CREATED,
    CREATE_REJECTED_ORDER_LIMIT,
    CREATE_REJECTED_CLOSED,
    UNCHANGED,
    COMPLETED,
    EXPIRED,
    CANCELLED,
    NOT_CANCELLABLE
}

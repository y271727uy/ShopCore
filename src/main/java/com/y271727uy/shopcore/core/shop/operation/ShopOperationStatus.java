package com.y271727uy.shopcore.core.shop.operation;

public enum ShopOperationStatus {
    OPENED,
    ALREADY_OPEN,
    CLOSED,
    ALREADY_CLOSED,
    DENIED_BY_MENU,
    DENIED_BY_OPENING_RULE,
    DENIED_BY_POLICY,
    DENIED_BY_ORDER_CAPACITY
}

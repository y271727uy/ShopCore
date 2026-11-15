package com.y271727uy.shopcore.api.component.order;

/** COMPLETED means all goods were delivered; inspect settlement separately. */
public enum DeliveryStatus { RECORDED, DELIVERED, COMPLETED, INPUT_EMPTY, NO_ORDER, ORDER_NOT_ACTIVE, ORDER_NOT_RECORDED, DELIVERY_NO_MATCH, DELIVERY_NOT_ALLOWED, DELIVERY_UNCHANGED }

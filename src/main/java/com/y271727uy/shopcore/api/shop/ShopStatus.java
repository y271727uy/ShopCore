package com.y271727uy.shopcore.api.shop;

/** Current operational status, including the last opening diagnostic translation key. */
public record ShopStatus(boolean openRequested, boolean open, int activeOrders, int totalOrders,
                         int consumers, String diagnostic) { }

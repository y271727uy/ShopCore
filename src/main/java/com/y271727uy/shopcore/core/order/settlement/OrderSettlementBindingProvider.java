package com.y271727uy.shopcore.core.order.settlement;

import com.y271727uy.shopcore.core.shop.instance.ShopInstance;

@FunctionalInterface
public interface OrderSettlementBindingProvider {
    OrderSettlementBinding bindingFor(ShopInstance shop);
}

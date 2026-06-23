package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.shop.instance.ShopInstance;

public interface ShopRuntimeHolder {
    ShopInstance shopcore$shopInstance();

    void shopcore$setShopInstance(ShopInstance shop);
}

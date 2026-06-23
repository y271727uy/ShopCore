package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;

public interface ShopMenuRuntimeHolder {
    ShopMenuSnapshot shopcore$menuSnapshot();

    void shopcore$setMenuSnapshot(ShopMenuSnapshot menuSnapshot);
}

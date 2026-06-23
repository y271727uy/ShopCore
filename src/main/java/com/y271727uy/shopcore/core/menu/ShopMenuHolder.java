package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

/**
 * Implement this on block entities or shop-backed components that own a concrete menu.
 */
public interface ShopMenuHolder {
    ShopId shopId();

    ShopMenuSnapshot menuSnapshot();

    void setMenuSnapshot(ShopMenuSnapshot snapshot);
}

package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.order.lifecycle.OrderStateChange;
import java.util.List;

public interface ShopBlockRuntimeHolder extends ShopRuntimeHolder, ShopOpenRequestHolder, ShopMenuRuntimeHolder, ShopOrderBookHolder {
    /** Called once after a committed runtime/order mutation. Plain data holders may leave this empty. */
    default void shopcore$runtimeChanged() { }

    /** State and statistics are already updated; observers must not perform settlement again. */
    default void shopcore$ordersChanged(List<OrderStateChange> changes) { }
}

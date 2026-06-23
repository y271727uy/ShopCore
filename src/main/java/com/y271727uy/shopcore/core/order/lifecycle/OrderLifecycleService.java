package com.y271727uy.shopcore.core.order.lifecycle;

import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.tier.ShopTierConfigs;

import java.util.List;
import java.util.Objects;

/**
 * State transitions for orders. Persistence, settlement and session stats are caller responsibilities.
 */
public final class OrderLifecycleService {
    private OrderLifecycleService() {
    }

    public static OrderLifecycleResult tryCreate(ShopInstance shop, List<ShopOrder> activeOrders, ShopOrder order) {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(activeOrders, "activeOrders");
        Objects.requireNonNull(order, "order");

        if (!shop.canAcceptOrders()) {
            return OrderLifecycleResult.rejected(OrderLifecycleStatus.CREATE_REJECTED_CLOSED);
        }
        int limit = ShopTierConfigs.get(shop.shopTier()).capacity().pendingOrderLimit();
        if (countActiveOrders(activeOrders) >= limit) {
            return OrderLifecycleResult.rejected(OrderLifecycleStatus.CREATE_REJECTED_ORDER_LIMIT);
        }
        return OrderLifecycleResult.created(order);
    }

    public static OrderLifecycleResult refresh(ShopOrder order, long gameTime) {
        Objects.requireNonNull(order, "order");
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }

        if (order.status() == OrderStatus.CANCELLED || order.status() == OrderStatus.EXPIRED) {
            return OrderLifecycleResult.unchanged(order);
        }

        if (order.status() != OrderStatus.COMPLETED && order.isExpired(gameTime)) {
            return OrderLifecycleResult.transitioned(OrderLifecycleStatus.EXPIRED, order, order.withStatus(OrderStatus.EXPIRED));
        }

        ShopOrder refreshed = order.refreshDeliveryStatus();
        if (refreshed.status() == order.status()) {
            return OrderLifecycleResult.unchanged(order);
        }
        if (refreshed.status() == OrderStatus.COMPLETED) {
            return OrderLifecycleResult.transitioned(OrderLifecycleStatus.COMPLETED, order, refreshed);
        }
        return OrderLifecycleResult.unchanged(refreshed);
    }

    public static OrderLifecycleResult cancel(ShopOrder order) {
        Objects.requireNonNull(order, "order");
        if (order.status() == OrderStatus.PENDING || order.status() == OrderStatus.PARTIAL) {
            return OrderLifecycleResult.transitioned(OrderLifecycleStatus.CANCELLED, order, order.withStatus(OrderStatus.CANCELLED));
        }
        return OrderLifecycleResult.notCancellable(order);
    }

    public static int countActiveOrders(List<ShopOrder> orders) {
        Objects.requireNonNull(orders, "orders");
        int count = 0;
        for (ShopOrder order : orders) {
            if (order != null && order.canReceiveDelivery()) {
                count++;
            }
        }
        return count;
    }
}

package com.y271727uy.shopcore.core.consumer;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.lifecycle.*;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeHolder;
import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import net.minecraft.core.BlockPos;

import java.util.*;

/** Headless accounting/notification tests. Item fulfillment and bank operations need a running game. */
public final class OrderLifecycleRegression {
    private static int checks;

    public static void main(String[] args) {
        statusNotifications();
        accounting();
        commitBoundary();
        emptyBookAndValidation();
        System.out.println("Order lifecycle accounting and commit boundaries passed: " + checks + " checks");
    }

    private static void statusNotifications() {
        UUID id = new UUID(0, 700);
        for (OrderStatus before : OrderStatus.values()) {
            for (OrderStatus after : OrderStatus.values()) {
                var changes = OrderStateChange.between(Map.of(id, before), Map.of(id, after));
                check(changes.size() == (before == after ? 0 : 1), "notify only actual status change");
                if (before != after) {
                    var change = changes.get(0);
                    equal(id, change.orderId(), "notification retains order identity");
                    boolean active = before == OrderStatus.PENDING || before == OrderStatus.PARTIAL;
                    boolean ended = after == OrderStatus.COMPLETED || after == OrderStatus.EXPIRED || after == OrderStatus.CANCELLED;
                    check(change.endsOrder() == (active && ended), "only active to terminal transitions end a consumer order");
                    check(OrderStateChange.between(Map.of(id, after), Map.of(id, after)).isEmpty(),
                            "repeated publication cannot repeat terminal notification");
                }
            }
        }
        var created = OrderStateChange.between(Map.of(), Map.of(id, OrderStatus.PENDING));
        check(created.size() == 1 && created.get(0).before() == null && !created.get(0).endsOrder(), "creation notification");
        check(OrderStateChange.between(Map.of(id, OrderStatus.COMPLETED), Map.of()).isEmpty(),
                "pruning history must not repeat completion or request settlement");
    }

    private static void accounting() {
        var original = BusinessSessionStats.open(0).recordOrderCompleted(100, 90, 10, 2);
        for (OrderLifecycleStatus status : OrderLifecycleStatus.values()) {
            var actual = OrderLifecycleAccounting.record(original, status);
            equal(original.ordersCreated() + (status == OrderLifecycleStatus.CREATED ? 1 : 0),
                    actual.ordersCreated(), "created statistics " + status);
            equal(original.ordersExpired() + (status == OrderLifecycleStatus.EXPIRED ? 1 : 0),
                    actual.ordersExpired(), "expiry statistics " + status);
            equal(original.ordersCancelled() + (status == OrderLifecycleStatus.CANCELLED ? 1 : 0),
                    actual.ordersCancelled(), "cancel statistics " + status);
            equal(original.ordersCompleted(), actual.ordersCompleted(), "status events cannot double count settlement");
            equal(original.grossRevenue(), actual.grossRevenue(), "status events do not invent revenue");
            equal(original.netRevenue(), actual.netRevenue(), "status events do not invent payout");
            equal(original.itemsSold(), actual.itemsSold(), "status events do not duplicate sold count");
        }
    }

    private static void commitBoundary() {
        Holder holder = new Holder();
        OrderLifecycleCommit.apply(holder, holder.shop, holder.book);
        check(holder.dirty == 0 && holder.notified == 0, "unchanged state is silent");
        ShopInstance changed = holder.shop.withCurrentSession(holder.shop.currentSession().recordOrderCancelled());
        OrderLifecycleCommit.apply(holder, changed, holder.book);
        check(holder.shop == changed && holder.dirty == 1, "new stats installed before save notification");
        check(holder.notified == 0, "statistics alone do not fabricate order transitions");
        OrderLifecycleCommit.apply(holder, changed, holder.book);
        check(holder.dirty == 1, "applying same result again is silent");
        var once = OrderLifecycleCommit.begin(holder);
        holder.shop = holder.shop.addReputation(1);
        once.close();
        once.close();
        check(holder.dirty == 2, "scope close is idempotent");

        RuntimeException failure = new RuntimeException("optional feedback failed");
        try (var commit = OrderLifecycleCommit.begin(holder)) {
            holder.shop = holder.shop.addReputation(1);
            throw failure;
        } catch (RuntimeException ex) {
            check(ex == failure, "original error propagates");
        }
        check(holder.dirty == 3 && holder.shop.reputation() == 2, "already applied state is saved even on failure");
        int[] custom = {0};
        try (var commit = OrderLifecycleCommit.begin(holder, () -> custom[0]++)) {
            holder.shop = holder.shop.addReputation(1);
        }
        check(custom[0] == 1 && holder.dirty == 3, "delivery-specific save hook is used exactly once");
    }

    private static void emptyBookAndValidation() {
        Holder holder = new Holder();
        var cancel = OrderBookLifecycleService.cancelActive(holder.shop, holder.book);
        check(cancel.shop() == holder.shop && cancel.orderBook() == holder.book && cancel.events().isEmpty(),
                "empty cancellation is a no-op");
        var refresh = OrderBookLifecycleService.refresh(holder.shop, holder.book, 50, Optional.empty());
        check(refresh.shop() == holder.shop && refresh.orderBook() == holder.book && refresh.events().isEmpty(),
                "empty refresh is a no-op");
        try {
            OrderBookLifecycleService.refresh(holder.shop, holder.book, -1, Optional.empty());
            throw new AssertionError("negative time accepted");
        } catch (IllegalArgumentException expected) { checks++; }
        var foreign = ShopOrderBook.empty(ShopInstance.create(new BlockPos(9, 64, 9)).shopId());
        try {
            OrderBookLifecycleService.cancelActive(holder.shop, foreign);
            throw new AssertionError("foreign shop book accepted");
        } catch (IllegalArgumentException expected) { checks++; }
    }

    private static void equal(Object expected, Object actual, String message) {
        check(Objects.equals(expected, actual), message + ": " + expected + " != " + actual);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static final class Holder implements ShopBlockRuntimeHolder {
        ShopInstance shop = ShopInstance.create(new BlockPos(0, 64, 0)).open(0);
        ShopOrderBook book = ShopOrderBook.empty(shop.shopId());
        int dirty, notified;
        public ShopInstance shopcore$shopInstance() { return shop; }
        public void shopcore$setShopInstance(ShopInstance value) { shop = value; }
        public ShopOrderBook shopcore$orderBook() { return book; }
        public void shopcore$setOrderBook(ShopOrderBook value) { book = value; }
        public boolean shopcore$openRequested() { return true; }
        public void shopcore$setOpenRequested(boolean value) { }
        public ShopMenuSnapshot shopcore$menuSnapshot() { return null; }
        public void shopcore$setMenuSnapshot(ShopMenuSnapshot value) { }
        public void shopcore$runtimeChanged() { dirty++; }
        public void shopcore$ordersChanged(List<OrderStateChange> changes) { notified++; }
    }
}

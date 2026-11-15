package com.y271727uy.shopcore.core.consumer.common.runtime;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.taking.OrderTakingController;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
public class OrderTakingLifecycle<A extends ConsumerActor> implements ConsumerLifecycle<A> {
    private final OrderTakingController<A> controller;
    private final Supplier<ShopOrderBook> orders;
    private final Runnable clearShopPrompts;
    public OrderTakingLifecycle(OrderTakingController<A> controller, Supplier<ShopOrderBook> orders, Runnable clearShopPrompts) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.clearShopPrompts = Objects.requireNonNull(clearShopPrompts, "clearShopPrompts");
    }
    public void tickWaiting(List<A> actors, ConsumerMemory memory, long time) { controller.tick(actors, memory, orders.get(), time); }
    public boolean request(A actor, ConsumerMemory memory) { return controller.request(actor, memory, orders.get()); }
    public void clearPrompt(A actor) { controller.clear(actor); }
    public void clearShopPrompts() { clearShopPrompts.run(); }
}

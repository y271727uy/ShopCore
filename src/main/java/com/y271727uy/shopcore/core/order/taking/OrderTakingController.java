package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
public final class OrderTakingController<A extends ConsumerActor> {
    private final OrderTakingBehavior behavior;
    private final OrderTakingPresentation<A> presentation;
    public OrderTakingController(OrderTakingBehavior behavior, OrderTakingPresentation<A> presentation) {
        this.behavior = Objects.requireNonNull(behavior, "behavior");
        this.presentation = Objects.requireNonNull(presentation, "presentation");
    }
    private Optional<ShopOrder> eligible(A actor, ConsumerMemory memory, ShopOrderBook orders) {
        if (memory.phase(actor) != QueueConsumerPhase.WAITING_FOR_ORDER) return Optional.empty();
        return memory.orderId(actor).flatMap(orders::find).filter(ShopOrder::canReceiveDelivery);
    }
    public void tick(List<A> actors, ConsumerMemory memory, ShopOrderBook orders, long time) {
        Objects.requireNonNull(orders, "orders");
        for (A actor : actors) eligible(actor, memory, orders).ifPresent(order ->
                behavior.tick(actor, memory, time, () -> presentation.waiting(actor), () -> presentation.accepted(actor, order)));
    }
    public boolean request(A actor, ConsumerMemory memory, ShopOrderBook orders) {
        return eligible(actor, memory, orders).map(order ->
                behavior.request(actor, memory, () -> presentation.accepted(actor, order))).orElse(false);
    }
    public void clear(A actor) {
        behavior.clear(actor);
        presentation.clear(actor);
    }
}

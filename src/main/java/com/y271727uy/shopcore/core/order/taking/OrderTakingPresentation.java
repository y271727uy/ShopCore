package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.order.ShopOrder;
/** Rendering only. Implementations must not advance the consumer phase. */
public interface OrderTakingPresentation<A extends ConsumerActor> {
    void waiting(A actor);
    void accepted(A actor, ShopOrder order);
    void clear(A actor);
}

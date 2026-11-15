package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
/** Called only for a waiting consumer with an active order. Outputs run before the phase transition. */
public interface OrderTakingBehavior {
    void tick(ConsumerActor actor, ConsumerMemory memory, long time, Runnable waiting, Runnable accepted);
    default boolean request(ConsumerActor actor, ConsumerMemory memory, Runnable accepted) { return false; }
    void clear(ConsumerActor actor);
}

package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
/** No automatic acceptance; the host explicitly calls OrderTakingController.request. */
public final class ManualOrderTaking implements OrderTakingBehavior {
    public void tick(ConsumerActor actor, ConsumerMemory memory, long time, Runnable waiting, Runnable accepted) {
        waiting.run();
    }
    public boolean request(ConsumerActor actor, ConsumerMemory memory, Runnable accepted) {
        accepted.run();
        memory.setPhase(actor, QueueConsumerPhase.WAITING_FOR_DELIVERY);
        return true;
    }
    public void clear(ConsumerActor actor) { }
}

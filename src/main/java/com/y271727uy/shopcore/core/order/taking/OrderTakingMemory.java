package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import java.util.OptionalLong;
public interface OrderTakingMemory {
    OptionalLong revealTime(ConsumerActor actor);
    void setRevealTime(ConsumerActor actor, long time);
    void clear(ConsumerActor actor);
}

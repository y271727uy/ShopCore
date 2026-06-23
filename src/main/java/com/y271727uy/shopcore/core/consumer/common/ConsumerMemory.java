package com.y271727uy.shopcore.core.consumer.common;

import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;

import java.util.Optional;
import java.util.UUID;

public interface ConsumerMemory {
    QueueConsumerPhase phase(ConsumerActor actor);

    void setPhase(ConsumerActor actor, QueueConsumerPhase phase);

    long joinedGameTime(ConsumerActor actor);

    void setJoinedGameTime(ConsumerActor actor, long gameTime);

    Optional<UUID> orderId(ConsumerActor actor);

    void setOrderId(ConsumerActor actor, UUID orderId);

    void clearOrderId(ConsumerActor actor);
}

package com.y271727uy.shopcore.core.consumer.common.wait;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;

import java.util.List;
import java.util.Objects;

public record QueueConsumerContext(
        QueueConsumerLayout layout,
        List<ConsumerActor> consumers,
        ConsumerNavigator navigator,
        ConsumerMemory memory,
        boolean shopOpen,
        long gameTime
) {
    public QueueConsumerContext {
        Objects.requireNonNull(layout, "layout");
        consumers = List.copyOf(Objects.requireNonNull(consumers, "consumers"));
        Objects.requireNonNull(navigator, "navigator");
        Objects.requireNonNull(memory, "memory");
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
    }
}

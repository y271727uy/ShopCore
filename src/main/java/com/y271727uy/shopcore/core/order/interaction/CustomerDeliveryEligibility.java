package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;

import java.util.Objects;
import java.util.Optional;

/** Non-mutating interaction guards. Order validity and item matching are checked by the delivery service. */
public final class CustomerDeliveryEligibility {
    private CustomerDeliveryEligibility() { }

    public static Optional<OrderInteractionStatus> rejection(boolean inputEmpty, ConsumerActor actor, ConsumerMemory memory) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(memory, "memory");
        if (inputEmpty) {
            return Optional.of(OrderInteractionStatus.INPUT_EMPTY);
        }
        if (memory.orderId(actor).isEmpty()) {
            return Optional.of(OrderInteractionStatus.NO_ORDER);
        }
        if (memory.phase(actor) == QueueConsumerPhase.WAITING_FOR_ORDER) {
            return Optional.of(OrderInteractionStatus.ORDER_NOT_RECORDED);
        }
        return Optional.empty();
    }
}

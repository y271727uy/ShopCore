package com.y271727uy.shopcore.core.consumer.common.wait;

public enum QueueConsumerPhase {
    ENTERING,
    QUEUING,
    WAITING_FOR_ORDER,
    WAITING_FOR_DELIVERY,
    LEAVING,
    DONE
}

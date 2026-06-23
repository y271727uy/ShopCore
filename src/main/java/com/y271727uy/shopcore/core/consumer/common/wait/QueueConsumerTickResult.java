package com.y271727uy.shopcore.core.consumer.common.wait;

public record QueueConsumerTickResult(
        int activeCount,
        int leavingCount,
        int doneCount
) {
    public QueueConsumerTickResult {
        if (activeCount < 0 || leavingCount < 0 || doneCount < 0) {
            throw new IllegalArgumentException("queue counts cannot be negative");
        }
    }
}

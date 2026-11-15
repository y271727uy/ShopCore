package com.y271727uy.shopcore.api.component.consumer;

import java.util.UUID;

/** Consumer ownership used by removal notifications and recovery after entity chunk unloads. */
public interface ConsumerOwnershipTarget {
    boolean ownsConsumer(UUID consumerId);
    default void consumerRemoved(UUID consumerId) { }

    /** Missing chunks do not imply missing shops. */
    static boolean shouldDetach(boolean ownerChunkLoaded, ConsumerOwnershipTarget owner, UUID consumerId) {
        return ownerChunkLoaded && (owner == null || !owner.ownsConsumer(consumerId));
    }
}

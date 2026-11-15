package com.y271727uy.shopcore.core.consumer.common.runtime;

/** A missing entity is unavailable, not evidence that it was permanently removed. */
public enum ConsumerPresence {
    LOADED,
    UNLOADED,
    REMOVED
}

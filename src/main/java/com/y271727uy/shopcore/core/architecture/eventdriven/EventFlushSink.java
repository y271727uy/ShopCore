package com.y271727uy.shopcore.core.architecture.eventdriven;

/**
 * Applies a drained event batch to a module-owned state store, SavedData, NBT payload, or network sync bridge.
 */
@FunctionalInterface
public interface EventFlushSink {
    void accept(EventFlushResult result);
}

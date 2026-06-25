package com.y271727uy.shopcore.core.architecture.eventdriven;

import java.util.List;
import java.util.Objects;

/**
 * Immutable batch drained from an accumulator.
 */
public record EventFlushResult(
        long flushedAtGameTime,
        List<EventDelta> deltas
) {
    public EventFlushResult {
        if (flushedAtGameTime < 0L) {
            throw new IllegalArgumentException("flushedAtGameTime cannot be negative");
        }
        deltas = List.copyOf(Objects.requireNonNull(deltas, "deltas").stream()
                .filter(delta -> !delta.isZero())
                .toList());
    }

    public static EventFlushResult empty(long flushedAtGameTime) {
        return new EventFlushResult(flushedAtGameTime, List.of());
    }

    public boolean isEmpty() {
        return deltas.isEmpty();
    }

    public long totalAbsoluteDelta() {
        long total = 0L;
        for (EventDelta delta : deltas) {
            total = saturatedAdd(total, Math.abs(delta.delta()));
        }
        return total;
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}

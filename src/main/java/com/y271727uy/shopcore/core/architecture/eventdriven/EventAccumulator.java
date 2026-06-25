package com.y271727uy.shopcore.core.architecture.eventdriven;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * In-memory delta accumulator for high-frequency events.
 *
 * <p>It deliberately does not persist, sync, or register itself anywhere. Owning modules decide when to record and
 * when to drain.</p>
 */
public class EventAccumulator {
    private final Map<EventBucketKey, Long> pendingDeltas = new LinkedHashMap<>();

    public void record(EventKey eventKey) {
        record(eventKey, 1L);
    }

    public void record(EventKey eventKey, long delta) {
        record(EventBucketKey.global(eventKey), delta);
    }

    public void record(EventBucketKey bucketKey) {
        record(bucketKey, 1L);
    }

    public void record(EventBucketKey bucketKey, long delta) {
        Objects.requireNonNull(bucketKey, "bucketKey");
        if (delta == 0L) {
            return;
        }
        pendingDeltas.merge(bucketKey, delta, EventAccumulator::saturatedAdd);
        if (pendingDeltas.get(bucketKey) == 0L) {
            pendingDeltas.remove(bucketKey);
        }
    }

    public boolean hasPending() {
        return !pendingDeltas.isEmpty();
    }

    public int bucketCount() {
        return pendingDeltas.size();
    }

    public List<EventDelta> pendingSnapshot() {
        return snapshotDeltas();
    }

    public EventFlushResult drain(long gameTime) {
        if (pendingDeltas.isEmpty()) {
            return EventFlushResult.empty(gameTime);
        }
        EventFlushResult result = new EventFlushResult(gameTime, snapshotDeltas());
        pendingDeltas.clear();
        return result;
    }

    public void clear() {
        pendingDeltas.clear();
    }

    private List<EventDelta> snapshotDeltas() {
        return pendingDeltas.entrySet().stream()
                .map(entry -> new EventDelta(entry.getKey(), entry.getValue()))
                .filter(delta -> !delta.isZero())
                .sorted(Comparator
                        .comparing((EventDelta delta) -> delta.bucketKey().eventKey().id().toString())
                        .thenComparing(delta -> delta.bucketKey().scope()))
                .toList();
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        if (right < 0L && left < Long.MIN_VALUE - right) {
            return Long.MIN_VALUE;
        }
        return left + right;
    }
}

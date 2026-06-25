package com.y271727uy.shopcore.core.architecture.eventdriven;

import java.util.Objects;

/**
 * One aggregated counter delta for a bucket.
 */
public record EventDelta(EventBucketKey bucketKey, long delta) {
    public EventDelta {
        Objects.requireNonNull(bucketKey, "bucketKey");
    }

    public boolean isZero() {
        return delta == 0L;
    }
}

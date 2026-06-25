package com.y271727uy.shopcore.core.architecture.eventdriven;

import java.util.Objects;

/**
 * Identifies where an event should be aggregated.
 *
 * <p>The scope can be a shop id, item id, player id, market id, or any other module-defined bucket.</p>
 */
public record EventBucketKey(EventKey eventKey, String scope) {
    public static final String GLOBAL_SCOPE = "global";

    public EventBucketKey {
        Objects.requireNonNull(eventKey, "eventKey");
        scope = normalizeScope(scope);
    }

    public static EventBucketKey global(EventKey eventKey) {
        return new EventBucketKey(eventKey, GLOBAL_SCOPE);
    }

    public static EventBucketKey scoped(EventKey eventKey, Object scope) {
        return new EventBucketKey(eventKey, String.valueOf(Objects.requireNonNull(scope, "scope")));
    }

    private static String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return GLOBAL_SCOPE;
        }
        return scope.trim();
    }
}

package com.y271727uy.shopcore.core.shop.session;

import java.util.Objects;
import java.util.UUID;

/**
 * Counters for the current business session only.
 * Lifetime income, reputation, tax exemption and ownership belong to other systems.
 */
public record BusinessSessionStats(
        UUID sessionId,
        long openedAtGameTime,
        long closedAtGameTime,
        long grossRevenue,
        long netRevenue,
        long taxPaid,
        int ordersCreated,
        int ordersCompleted,
        int ordersExpired,
        int ordersCancelled,
        int itemsSold
) {
    private static final UUID EMPTY_SESSION_ID = new UUID(0L, 0L);
    private static final long NOT_STARTED = -1L;

    public BusinessSessionStats {
        Objects.requireNonNull(sessionId, "sessionId");
        if (openedAtGameTime < NOT_STARTED) {
            throw new IllegalArgumentException("openedAtGameTime cannot be less than -1");
        }
        if (closedAtGameTime < NOT_STARTED) {
            throw new IllegalArgumentException("closedAtGameTime cannot be less than -1");
        }
        if (openedAtGameTime == NOT_STARTED && closedAtGameTime != NOT_STARTED) {
            throw new IllegalArgumentException("closedAtGameTime requires a started session");
        }
        if (openedAtGameTime >= 0L && closedAtGameTime >= 0L && closedAtGameTime < openedAtGameTime) {
            throw new IllegalArgumentException("closedAtGameTime cannot be before openedAtGameTime");
        }
        if (grossRevenue < 0L || netRevenue < 0L || taxPaid < 0L) {
            throw new IllegalArgumentException("revenue values cannot be negative");
        }
        if (ordersCreated < 0 || ordersCompleted < 0 || ordersExpired < 0 || ordersCancelled < 0 || itemsSold < 0) {
            throw new IllegalArgumentException("session counters cannot be negative");
        }
    }

    public static BusinessSessionStats empty() {
        return new BusinessSessionStats(EMPTY_SESSION_ID, NOT_STARTED, NOT_STARTED, 0L, 0L, 0L, 0, 0, 0, 0, 0);
    }

    public static BusinessSessionStats open(long gameTime) {
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        return new BusinessSessionStats(UUID.randomUUID(), gameTime, NOT_STARTED, 0L, 0L, 0L, 0, 0, 0, 0, 0);
    }

    public boolean hasStarted() {
        return openedAtGameTime != NOT_STARTED;
    }

    public boolean isOpen() {
        return hasStarted() && closedAtGameTime == NOT_STARTED;
    }

    public BusinessSessionStats close(long gameTime) {
        if (!hasStarted()) {
            return this;
        }
        if (gameTime < openedAtGameTime) {
            throw new IllegalArgumentException("gameTime cannot be before openedAtGameTime");
        }
        return new BusinessSessionStats(
                sessionId,
                openedAtGameTime,
                gameTime,
                grossRevenue,
                netRevenue,
                taxPaid,
                ordersCreated,
                ordersCompleted,
                ordersExpired,
                ordersCancelled,
                itemsSold
        );
    }

    public BusinessSessionStats recordOrderCreated() {
        return new BusinessSessionStats(
                sessionId,
                openedAtGameTime,
                closedAtGameTime,
                grossRevenue,
                netRevenue,
                taxPaid,
                ordersCreated + 1,
                ordersCompleted,
                ordersExpired,
                ordersCancelled,
                itemsSold
        );
    }

    public BusinessSessionStats recordOrderCompleted(long grossRevenueDelta, long netRevenueDelta, long taxPaidDelta, int itemsSoldDelta) {
        if (grossRevenueDelta < 0L || netRevenueDelta < 0L || taxPaidDelta < 0L) {
            throw new IllegalArgumentException("revenue deltas cannot be negative");
        }
        if (itemsSoldDelta < 0) {
            throw new IllegalArgumentException("itemsSoldDelta cannot be negative");
        }
        return new BusinessSessionStats(
                sessionId,
                openedAtGameTime,
                closedAtGameTime,
                grossRevenue + grossRevenueDelta,
                netRevenue + netRevenueDelta,
                taxPaid + taxPaidDelta,
                ordersCreated,
                ordersCompleted + 1,
                ordersExpired,
                ordersCancelled,
                itemsSold + itemsSoldDelta
        );
    }

    public BusinessSessionStats recordOrderExpired() {
        return new BusinessSessionStats(
                sessionId,
                openedAtGameTime,
                closedAtGameTime,
                grossRevenue,
                netRevenue,
                taxPaid,
                ordersCreated,
                ordersCompleted,
                ordersExpired + 1,
                ordersCancelled,
                itemsSold
        );
    }

    public BusinessSessionStats recordOrderCancelled() {
        return new BusinessSessionStats(
                sessionId,
                openedAtGameTime,
                closedAtGameTime,
                grossRevenue,
                netRevenue,
                taxPaid,
                ordersCreated,
                ordersCompleted,
                ordersExpired,
                ordersCancelled + 1,
                itemsSold
        );
    }
}

package com.y271727uy.shopcore.core.architecture.eventdriven;

import java.util.Objects;
import java.util.Optional;

/**
 * Small tick-based flush helper for modules that want periodic aggregation without binding to a global event bus.
 */
public class TickFlushScheduler {
    private final long intervalTicks;
    private long lastFlushGameTime = -1L;

    public TickFlushScheduler(long intervalTicks) {
        if (intervalTicks <= 0L) {
            throw new IllegalArgumentException("intervalTicks must be positive");
        }
        this.intervalTicks = intervalTicks;
    }

    public long intervalTicks() {
        return intervalTicks;
    }

    public long lastFlushGameTime() {
        return lastFlushGameTime;
    }

    public boolean shouldFlush(long gameTime) {
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        return lastFlushGameTime < 0L || gameTime - lastFlushGameTime >= intervalTicks;
    }

    public void markFlushed(long gameTime) {
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        lastFlushGameTime = gameTime;
    }

    public Optional<EventFlushResult> flushIfDue(EventAccumulator accumulator, long gameTime) {
        Objects.requireNonNull(accumulator, "accumulator");
        if (!shouldFlush(gameTime)) {
            return Optional.empty();
        }
        markFlushed(gameTime);
        if (!accumulator.hasPending()) {
            return Optional.empty();
        }
        return Optional.of(accumulator.drain(gameTime));
    }

    public Optional<EventFlushResult> flushIfDue(EventAccumulator accumulator, long gameTime, EventFlushSink sink) {
        Objects.requireNonNull(sink, "sink");
        Optional<EventFlushResult> result = flushIfDue(accumulator, gameTime);
        result.ifPresent(sink::accept);
        return result;
    }
}

package com.y271727uy.shopcore.core.shop.policy;

import java.util.Objects;

public record OperatingPolicyContext(
        OperatingPolicyKey policyKey,
        long dayTime,
        boolean manualOpen
) {
    public static final long DAY_LENGTH_TICKS = 24_000L;
    public static final long DAY_START_TICK = 0L;
    public static final long NIGHT_START_TICK = 12_000L;

    public OperatingPolicyContext {
        Objects.requireNonNull(policyKey, "policyKey");
        if (dayTime < 0L) {
            throw new IllegalArgumentException("dayTime cannot be negative");
        }
    }

    public long normalizedDayTime() {
        return dayTime % DAY_LENGTH_TICKS;
    }

    public boolean isDay() {
        long time = normalizedDayTime();
        return time >= DAY_START_TICK && time < NIGHT_START_TICK;
    }

    public boolean isNight() {
        return !isDay();
    }
}

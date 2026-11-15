package com.y271727uy.shopcore.api.component.order;

/** Order creation interval and lifetime, measured in game ticks. */
public record OrderSettings(long generationIntervalTicks, long lifetimeTicks) {
    public OrderSettings {
        if (generationIntervalTicks < 1) throw new IllegalArgumentException("generationIntervalTicks must be positive");
        if (lifetimeTicks < 1) throw new IllegalArgumentException("lifetimeTicks must be positive");
    }
}

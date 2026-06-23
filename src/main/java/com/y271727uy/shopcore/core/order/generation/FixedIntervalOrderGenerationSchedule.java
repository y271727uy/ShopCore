package com.y271727uy.shopcore.core.order.generation;

import java.util.Objects;

public record FixedIntervalOrderGenerationSchedule(
        long intervalTicks,
        double attemptChance
) implements OrderGenerationSchedule {
    public FixedIntervalOrderGenerationSchedule {
        if (intervalTicks < 1L) {
            throw new IllegalArgumentException("intervalTicks must be at least 1");
        }
        if (!Double.isFinite(attemptChance) || attemptChance < 0.0D || attemptChance > 1.0D) {
            throw new IllegalArgumentException("attemptChance must be in [0, 1]");
        }
    }

    public static FixedIntervalOrderGenerationSchedule every(long intervalTicks) {
        return new FixedIntervalOrderGenerationSchedule(intervalTicks, 1.0D);
    }

    @Override
    public OrderGenerationScheduleDecision evaluate(OrderGenerationScheduleContext context) {
        Objects.requireNonNull(context, "context");
        if (context.gameTime() % intervalTicks != 0L) {
            return OrderGenerationScheduleDecision.denied(OrderGenerationScheduleDecision.REASON_INTERVAL_WAIT);
        }
        if (attemptChance < 1.0D && context.random().nextDouble() > attemptChance) {
            return OrderGenerationScheduleDecision.denied(OrderGenerationScheduleDecision.REASON_RANDOM_CHANCE_FAILED);
        }
        return OrderGenerationScheduleDecision.allowed();
    }
}

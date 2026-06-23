package com.y271727uy.shopcore.core.order.generation;

public interface OrderGenerationSchedule {
    OrderGenerationSchedule ALWAYS = context -> OrderGenerationScheduleDecision.allowed();
    OrderGenerationSchedule NEVER = context -> OrderGenerationScheduleDecision.denied(OrderGenerationScheduleDecision.REASON_DISABLED);

    OrderGenerationScheduleDecision evaluate(OrderGenerationScheduleContext context);
}

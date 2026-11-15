package com.y271727uy.shopcore.core.order.taking;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import java.util.Objects;
import java.util.OptionalLong;
public final class AutomaticOrderTaking implements OrderTakingBehavior {
    private final long waitTicks;
    private final OrderTakingMemory timing;
    public AutomaticOrderTaking(long waitTicks, OrderTakingMemory timing) {
        if (waitTicks < 0) throw new IllegalArgumentException("waitTicks cannot be negative");
        this.waitTicks = waitTicks;
        this.timing = Objects.requireNonNull(timing, "timing");
    }
    public void tick(ConsumerActor actor, ConsumerMemory memory, long time, Runnable waiting, Runnable accepted) {
        OptionalLong reveal = timing.revealTime(actor);
        if (reveal.isEmpty()) {
            timing.setRevealTime(actor, time + waitTicks);
            waiting.run();
            return; // Preserve the original first-tick wait, even when waitTicks is zero.
        }
        if (time < reveal.getAsLong()) {
            waiting.run();
            return;
        }
        accepted.run();
        timing.clear(actor);
        memory.setPhase(actor, QueueConsumerPhase.WAITING_FOR_DELIVERY);
    }
    public void clear(ConsumerActor actor) { timing.clear(actor); }
}

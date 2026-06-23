package com.y271727uy.shopcore.core.consumer.common.wait;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Counter-style queue behavior inspired by Skyblock Burgeria.
 * The first consumer moves to the service point; later consumers occupy queue slots behind it.
 */
public class QueueConsumerBehavior {
    public QueueConsumerTickResult tick(QueueConsumerContext context) {
        Objects.requireNonNull(context, "context");
        ConsumerMemory memory = context.memory();
        ConsumerNavigator navigator = context.navigator();

        List<ConsumerActor> alive = context.consumers().stream()
                .filter(ConsumerActor::isAlive)
                .toList();

        if (!context.shopOpen()) {
            for (ConsumerActor actor : alive) {
                QueueConsumerPhase phase = normalizePhase(memory, actor, context.gameTime());
                if (phase != QueueConsumerPhase.DONE) {
                    memory.setPhase(actor, QueueConsumerPhase.LEAVING);
                }
            }
        }

        List<ConsumerActor> queue = alive.stream()
                .filter(actor -> {
                    QueueConsumerPhase phase = normalizePhase(memory, actor, context.gameTime());
                    return phase != QueueConsumerPhase.LEAVING && phase != QueueConsumerPhase.DONE;
                })
                .sorted(Comparator.comparingLong(memory::joinedGameTime).thenComparing(ConsumerActor::consumerId))
                .toList();

        for (int i = 0; i < queue.size(); i++) {
            ConsumerActor actor = queue.get(i);
            if (i == 0) {
                tickFrontConsumer(context, actor);
            } else {
                tickQueuedConsumer(context, actor, i - 1);
            }
        }

        int leaving = 0;
        int done = 0;
        for (ConsumerActor actor : alive) {
            QueueConsumerPhase phase = normalizePhase(memory, actor, context.gameTime());
            if (phase == QueueConsumerPhase.LEAVING) {
                leaving++;
                tickLeavingConsumer(context, actor);
            } else if (phase == QueueConsumerPhase.DONE) {
                done++;
                navigator.stop(actor);
            }
        }

        return new QueueConsumerTickResult(queue.size(), leaving, done);
    }

    private void tickFrontConsumer(QueueConsumerContext context, ConsumerActor actor) {
        ConsumerMemory memory = context.memory();
        QueueConsumerPhase phase = normalizePhase(memory, actor, context.gameTime());
        if (phase == QueueConsumerPhase.WAITING_FOR_ORDER || phase == QueueConsumerPhase.WAITING_FOR_DELIVERY) {
            waitAtService(context, actor);
            return;
        }

        Vec3 serviceTarget = context.layout().transform().toWorldGroundCenter(context.layout().serviceSlot().localOffset());
        if (context.navigator().isCloseTo(actor, serviceTarget, context.layout().arrivalDistance())) {
            memory.setPhase(actor, QueueConsumerPhase.WAITING_FOR_ORDER);
            waitAtService(context, actor);
            return;
        }

        memory.setPhase(actor, QueueConsumerPhase.ENTERING);
        context.navigator().moveTowards(actor, serviceTarget, context.layout().moveSpeed());
    }

    private void tickQueuedConsumer(QueueConsumerContext context, ConsumerActor actor, int queueIndex) {
        Vec3 target = context.layout().transform().toWorldGroundCenter(context.layout().queueSlotForIndex(queueIndex).localOffset());
        if (context.navigator().isCloseTo(actor, target, context.layout().arrivalDistance())) {
            context.memory().setPhase(actor, QueueConsumerPhase.QUEUING);
            context.navigator().stop(actor);
            context.navigator().face(actor, context.layout().serviceFacing());
            return;
        }

        context.memory().setPhase(actor, QueueConsumerPhase.QUEUING);
        context.navigator().moveTowards(actor, target, context.layout().moveSpeed());
    }

    private void tickLeavingConsumer(QueueConsumerContext context, ConsumerActor actor) {
        Vec3 exitTarget = context.layout().transform().toWorldGroundCenter(context.layout().exitSlot().localOffset());
        if (context.navigator().isCloseTo(actor, exitTarget, context.layout().arrivalDistance())) {
            context.memory().setPhase(actor, QueueConsumerPhase.DONE);
            context.navigator().discard(actor);
            return;
        }

        context.navigator().moveTowards(actor, exitTarget, context.layout().moveSpeed());
    }

    private void waitAtService(QueueConsumerContext context, ConsumerActor actor) {
        context.navigator().stop(actor);
        context.navigator().face(actor, context.layout().serviceFacing());
    }

    private QueueConsumerPhase normalizePhase(ConsumerMemory memory, ConsumerActor actor, long gameTime) {
        QueueConsumerPhase phase = memory.phase(actor);
        if (phase == null) {
            memory.setPhase(actor, QueueConsumerPhase.ENTERING);
            memory.setJoinedGameTime(actor, gameTime);
            return QueueConsumerPhase.ENTERING;
        }
        return phase;
    }

}

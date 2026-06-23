package com.y271727uy.shopcore.core.consumer.common.wait;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutRole;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutSlot;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutTransform;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * One-line counter queue.
 * Customers enter, wait, and leave on the same line south of the counter.
 */
public class HorizontalQueue {
    private static final double SPAWN_X = 8.0D;
    private static final double EXIT_X = -10.0D;
    private static final double SERVICE_START_X = 0.5D;
    private static final double SERVICE_Z = 0.5D;
    private static final double QUEUE_SPACING = 1.0D;
    private static final double STEP_TARGET = 1.0D;
    private static final double SLOT_EPSILON = 0.08D;

    public QueueConsumerTickResult tick(HorizontalQueueContext context) {
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

        List<ConsumerActor> active = alive.stream()
                .filter(actor -> {
                    QueueConsumerPhase phase = normalizePhase(memory, actor, context.gameTime());
                    return phase != QueueConsumerPhase.LEAVING && phase != QueueConsumerPhase.DONE;
                })
                .sorted(Comparator.comparingLong(memory::joinedGameTime).thenComparing(ConsumerActor::consumerId))
                .toList();

        int serviceSlots = context.layout().serviceSlots().size();
        for (int i = 0; i < active.size(); i++) {
            ConsumerActor actor = active.get(i);
            if (i < serviceSlots) {
                tickServiceConsumer(context, actor, context.layout().serviceLocalPosition(i));
            } else {
                tickQueuedConsumer(context, actor, context.layout().queueLocalPosition(i - serviceSlots));
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

        return new QueueConsumerTickResult(active.size(), leaving, done);
    }

    private void tickServiceConsumer(HorizontalQueueContext context, ConsumerActor actor, Vec3 serviceLocal) {
        ConsumerMemory memory = context.memory();
        QueueConsumerPhase phase = normalizePhase(memory, actor, context.gameTime());
        if (phase == QueueConsumerPhase.WAITING_FOR_ORDER || phase == QueueConsumerPhase.WAITING_FOR_DELIVERY) {
            waitAtService(context, actor);
            return;
        }

        Vec3 local = context.layout().toLocal(actor);
        if (isClose(local.x, serviceLocal.x) && isClose(local.z, serviceLocal.z)) {
            memory.setPhase(actor, QueueConsumerPhase.WAITING_FOR_ORDER);
            waitAtService(context, actor);
            return;
        }

        memory.setPhase(actor, QueueConsumerPhase.ENTERING);
        moveAlongServiceLine(context, actor, serviceLocal.x);
    }

    private void tickQueuedConsumer(HorizontalQueueContext context, ConsumerActor actor, Vec3 queueLocal) {
        Vec3 local = context.layout().toLocal(actor);
        if (isClose(local.x, queueLocal.x) && isClose(local.z, queueLocal.z)) {
            context.memory().setPhase(actor, QueueConsumerPhase.QUEUING);
            context.navigator().stop(actor);
            return;
        }

        context.memory().setPhase(actor, QueueConsumerPhase.QUEUING);
        moveAlongServiceLine(context, actor, queueLocal.x);
    }

    private void tickLeavingConsumer(HorizontalQueueContext context, ConsumerActor actor) {
        Vec3 local = context.layout().toLocal(actor);
        if (local.x <= EXIT_X + SLOT_EPSILON && isClose(local.z, SERVICE_Z)) {
            context.memory().setPhase(actor, QueueConsumerPhase.DONE);
            context.navigator().discard(actor);
            return;
        }

        moveAlongServiceLine(context, actor, EXIT_X);
    }

    private void waitAtService(HorizontalQueueContext context, ConsumerActor actor) {
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

    private void moveAlongServiceLine(HorizontalQueueContext context, ConsumerActor actor, double targetX) {
        Vec3 local = context.layout().toLocal(actor);
        double nextX = moveToward(local.x, targetX);
        double nextZ = moveToward(local.z, SERVICE_Z);
        Vec3 target = context.layout().transform().toWorldPosition(new Vec3(nextX, local.y, nextZ));
        context.navigator().moveTowards(actor, target, context.layout().moveSpeed());
    }

    private double moveToward(double current, double target) {
        if (current < target) {
            return Math.min(target, current + STEP_TARGET);
        }
        return Math.max(target, current - STEP_TARGET);
    }

    private boolean isClose(double current, double target) {
        return Math.abs(current - target) <= SLOT_EPSILON;
    }

    public record HorizontalQueueContext(
            HorizontalQueueLayout layout,
            List<ConsumerActor> consumers,
            ConsumerNavigator navigator,
            ConsumerMemory memory,
            boolean shopOpen,
            long gameTime
    ) {
        public HorizontalQueueContext {
            Objects.requireNonNull(layout, "layout");
            consumers = List.copyOf(Objects.requireNonNull(consumers, "consumers"));
            Objects.requireNonNull(navigator, "navigator");
            Objects.requireNonNull(memory, "memory");
            if (gameTime < 0L) {
                throw new IllegalArgumentException("gameTime cannot be negative");
            }
        }
    }

    public record HorizontalQueueLayout(
            ConsumerLayoutTransform transform,
            List<ConsumerLayoutSlot> serviceSlots,
            List<ConsumerLayoutSlot> queueSlots,
            ConsumerLayoutSlot exitSlot,
            Direction serviceFacing,
            double moveSpeed,
            double arrivalDistance
    ) {
        public HorizontalQueueLayout {
            Objects.requireNonNull(transform, "transform");
            serviceSlots = List.copyOf(Objects.requireNonNull(serviceSlots, "serviceSlots").stream()
                    .sorted(Comparator.comparingInt(ConsumerLayoutSlot::index))
                    .toList());
            if (serviceSlots.isEmpty()) {
                throw new IllegalArgumentException("serviceSlots cannot be empty");
            }
            for (ConsumerLayoutSlot slot : serviceSlots) {
                if (slot.role() != ConsumerLayoutRole.SERVICE) {
                    throw new IllegalArgumentException("service slot must have SERVICE role");
                }
            }
            queueSlots = List.copyOf(Objects.requireNonNull(queueSlots, "queueSlots").stream()
                    .sorted(Comparator.comparingInt(ConsumerLayoutSlot::index))
                    .toList());
            for (ConsumerLayoutSlot slot : queueSlots) {
                if (slot.role() != ConsumerLayoutRole.QUEUE) {
                    throw new IllegalArgumentException("queue slot must have QUEUE role");
                }
            }
            Objects.requireNonNull(exitSlot, "exitSlot");
            if (exitSlot.role() != ConsumerLayoutRole.EXIT) {
                throw new IllegalArgumentException("exitSlot must have EXIT role");
            }
            Objects.requireNonNull(serviceFacing, "serviceFacing");
            if (serviceFacing.getAxis().isVertical()) {
                throw new IllegalArgumentException("serviceFacing must be horizontal");
            }
            if (!Double.isFinite(moveSpeed) || moveSpeed <= 0.0D) {
                throw new IllegalArgumentException("moveSpeed must be positive");
            }
            if (!Double.isFinite(arrivalDistance) || arrivalDistance <= 0.0D) {
                throw new IllegalArgumentException("arrivalDistance must be positive");
            }
        }

        public Vec3 toLocal(ConsumerActor actor) {
            return transform.toLocalPosition(actor.position());
        }

        public Vec3 serviceLocalPosition(int serviceIndex) {
            return new Vec3(SERVICE_START_X + serviceIndex, 0.0D, SERVICE_Z);
        }

        public Vec3 queueLocalPosition(int queueIndex) {
            return new Vec3(SERVICE_START_X + serviceSlots.size() + queueIndex * QUEUE_SPACING, 0.0D, SERVICE_Z);
        }

        public Vec3 spawnPosition() {
            return transform.toWorldPosition(new Vec3(SPAWN_X, 0.0D, SERVICE_Z));
        }
    }
}

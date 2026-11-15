package com.y271727uy.shopcore.core.consumer.common.movement;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutRole;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutSlot;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutTransform;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue.HorizontalQueueContext;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue.HorizontalQueueLayout;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerBehavior;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerContext;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Adapters over the existing scripts. Their movement and phase-transition rules are unchanged. */
public final class ConsumerMovements {
    private ConsumerMovements() {
    }

    /** The existing test-shop preset: one service slot, three queue slots, speed 1.0. */
    public static ConsumerMovement horizontalQueue(BlockPos origin, Direction facing) {
        return horizontalQueue(new HorizontalQueueLayout(
                new ConsumerLayoutTransform(origin, facing),
                List.of(new ConsumerLayoutSlot(ConsumerLayoutRole.SERVICE, 0, BlockPos.ZERO)),
                List.of(new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 0, BlockPos.ZERO),
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 1, BlockPos.ZERO),
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 2, BlockPos.ZERO)),
                new ConsumerLayoutSlot(ConsumerLayoutRole.EXIT, 0, BlockPos.ZERO),
                facing, 1.0D, 0.85D));
    }

    public static ConsumerMovement horizontalQueue(HorizontalQueueLayout layout) {
        Objects.requireNonNull(layout, "layout");
        HorizontalQueue behavior = new HorizontalQueue();
        return new ConsumerMovement() {
            @Override
            public Vec3 spawnPosition() {
                return layout.spawnPosition();
            }

            @Override
            public boolean entranceClear(List<ConsumerActor> actors) {
                return actors.stream().map(layout::toLocal).noneMatch(local -> local.x >= 7.0D);
            }

            @Override
            public void tick(List<ConsumerActor> actors, ConsumerNavigator navigator, ConsumerMemory memory,
                             boolean shopOpen, long gameTime) {
                behavior.tick(new HorizontalQueueContext(layout, actors, navigator, memory, shopOpen, gameTime));
            }
        };
    }

    /** The other existing script, with shop-selected spawning and admission rules. */
    public static ConsumerMovement counterQueue(QueueConsumerLayout layout, Vec3 spawnPosition,
                                                 Predicate<List<ConsumerActor>> entranceClear) {
        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(spawnPosition, "spawnPosition");
        Objects.requireNonNull(entranceClear, "entranceClear");
        QueueConsumerBehavior behavior = new QueueConsumerBehavior();
        return new ConsumerMovement() {
            @Override
            public Vec3 spawnPosition() {
                return spawnPosition;
            }

            @Override
            public boolean entranceClear(List<ConsumerActor> actors) {
                return entranceClear.test(actors);
            }

            @Override
            public void tick(List<ConsumerActor> actors, ConsumerNavigator navigator, ConsumerMemory memory,
                             boolean shopOpen, long gameTime) {
                behavior.tick(new QueueConsumerContext(layout, actors, navigator, memory, shopOpen, gameTime));
            }
        };
    }
}

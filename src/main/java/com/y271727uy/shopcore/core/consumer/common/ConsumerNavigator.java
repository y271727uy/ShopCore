package com.y271727uy.shopcore.core.consumer.common;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public interface ConsumerNavigator {
    void moveTowards(ConsumerActor actor, Vec3 target, double speed);

    void stop(ConsumerActor actor);

    void face(ConsumerActor actor, Direction direction);

    void discard(ConsumerActor actor);

    default boolean isCloseTo(ConsumerActor actor, Vec3 target, double distance) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(target, "target");
        return actor.position().distanceToSqr(target) <= distance * distance;
    }
}

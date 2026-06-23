package com.y271727uy.shopcore.core.consumer.common;

import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public interface ConsumerActor {
    UUID consumerId();

    Vec3 position();

    boolean isAlive();
}

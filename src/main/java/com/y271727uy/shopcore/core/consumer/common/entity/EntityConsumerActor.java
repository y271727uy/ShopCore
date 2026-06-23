package com.y271727uy.shopcore.core.consumer.common.entity;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public record EntityConsumerActor(Entity entity) implements EntityBackedConsumerActor {
    public EntityConsumerActor {
        Objects.requireNonNull(entity, "entity");
    }

    @Override
    public UUID consumerId() {
        return entity.getUUID();
    }

    @Override
    public Vec3 position() {
        return entity.position();
    }

    @Override
    public boolean isAlive() {
        return entity.isAlive() && !entity.isRemoved();
    }
}

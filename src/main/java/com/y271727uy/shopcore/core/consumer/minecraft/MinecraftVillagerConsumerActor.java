package com.y271727uy.shopcore.core.consumer.minecraft;

import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public record MinecraftVillagerConsumerActor(Villager villager) implements EntityBackedConsumerActor {
    public MinecraftVillagerConsumerActor {
        Objects.requireNonNull(villager, "villager");
    }

    @Override
    public UUID consumerId() {
        return villager.getUUID();
    }

    @Override
    public Vec3 position() {
        return villager.position();
    }

    @Override
    public boolean isAlive() {
        return villager.isAlive() && !villager.isRemoved();
    }

    @Override
    public Entity entity() {
        return villager;
    }
}

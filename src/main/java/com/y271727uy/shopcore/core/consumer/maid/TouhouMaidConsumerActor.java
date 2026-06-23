package com.y271727uy.shopcore.core.consumer.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public record TouhouMaidConsumerActor(EntityMaid maid) implements EntityBackedConsumerActor {
    public TouhouMaidConsumerActor {
        Objects.requireNonNull(maid, "maid");
    }

    @Override
    public UUID consumerId() {
        return maid.getUUID();
    }

    @Override
    public Vec3 position() {
        return maid.position();
    }

    @Override
    public boolean isAlive() {
        return maid.isAlive() && !maid.isRemoved();
    }

    @Override
    public Entity entity() {
        return maid;
    }
}

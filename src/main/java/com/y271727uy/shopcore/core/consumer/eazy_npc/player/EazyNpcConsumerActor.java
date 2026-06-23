package com.y271727uy.shopcore.core.consumer.eazy_npc.player;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import de.markusbordihn.easynpc.entity.easynpc.EasyNPC;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.UUID;

public record EazyNpcConsumerActor(EasyNPC<?> npc) implements EntityBackedConsumerActor {
    public EazyNpcConsumerActor {
        Objects.requireNonNull(npc, "npc");
    }

    @Override
    public UUID consumerId() {
        return npc.getUUID();
    }

    @Override
    public Vec3 position() {
        return entity().position();
    }

    @Override
    public boolean isAlive() {
        Entity entity = entity();
        return entity.isAlive() && !entity.isRemoved();
    }

    public Entity entity() {
        return npc.getEntity();
    }
}

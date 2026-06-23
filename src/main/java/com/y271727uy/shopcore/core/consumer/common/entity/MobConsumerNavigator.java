package com.y271727uy.shopcore.core.consumer.common.entity;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class MobConsumerNavigator implements ConsumerNavigator {
    @Override
    public void moveTowards(ConsumerActor actor, Vec3 target, double speed) {
        Objects.requireNonNull(target, "target");
        Mob mob = requireMob(actor);
        PathNavigation navigation = mob.getNavigation();
        navigation.moveTo(target.x, target.y, target.z, speed);
    }

    @Override
    public void stop(ConsumerActor actor) {
        requireMob(actor).getNavigation().stop();
    }

    @Override
    public void face(ConsumerActor actor, Direction direction) {
        Objects.requireNonNull(direction, "direction");
        Mob mob = requireMob(actor);
        float yaw = direction.toYRot();
        mob.setYRot(yaw);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
    }

    @Override
    public void discard(ConsumerActor actor) {
        requireEntity(actor).discard();
    }

    protected Mob requireMob(ConsumerActor actor) {
        Entity entity = requireEntity(actor);
        if (entity instanceof Mob mob) {
            return mob;
        }
        throw new IllegalArgumentException("actor entity must be a Mob");
    }

    protected Entity requireEntity(ConsumerActor actor) {
        Objects.requireNonNull(actor, "actor");
        if (actor instanceof EntityBackedConsumerActor entityActor) {
            return entityActor.entity();
        }
        throw new IllegalArgumentException("actor must be an EntityBackedConsumerActor");
    }
}

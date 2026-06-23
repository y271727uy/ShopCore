package com.y271727uy.shopcore.core.consumer.common.entity;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class ManualMobConsumerNavigator extends MobConsumerNavigator {
    private static final double BASE_STEP_PER_TICK = 0.1D;

    @Override
    public void moveTowards(ConsumerActor actor, Vec3 target, double speed) {
        Objects.requireNonNull(target, "target");
        Mob mob = requireMob(actor);
        mob.setNoAi(true);
        mob.setDeltaMovement(Vec3.ZERO);

        Vec3 position = mob.position();
        Vec3 delta = target.subtract(position);
        double distance = delta.length();
        if (distance <= 1.0E-4D) {
            stop(actor);
            return;
        }

        double step = Math.min(distance, BASE_STEP_PER_TICK * Math.max(0.1D, speed));
        Vec3 next = position.add(delta.normalize().scale(step));
        float yaw = (float) (Math.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
        mob.moveTo(next.x, next.y, next.z, yaw, mob.getXRot());
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
    }

    @Override
    public void stop(ConsumerActor actor) {
        Mob mob = requireMob(actor);
        mob.setNoAi(true);
        mob.setDeltaMovement(Vec3.ZERO);
    }
}

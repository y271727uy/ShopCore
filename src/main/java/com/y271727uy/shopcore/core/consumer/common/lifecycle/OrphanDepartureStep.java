package com.y271727uy.shopcore.core.consumer.common.lifecycle;

import net.minecraft.world.phys.Vec3;

/** The original detached-consumer movement rule, shared by all supported Mob types. */
public record OrphanDepartureStep(boolean finished, Vec3 position, float yaw) {
    public static OrphanDepartureStep towards(Vec3 position, Vec3 target) {
        Vec3 delta = target.subtract(position);
        double distance = delta.length();
        if (!Double.isFinite(distance) || distance <= 0.12D) {
            return new OrphanDepartureStep(true, position, 0.0F);
        }
        double step = Math.min(distance, 0.1D);
        Vec3 next = position.add(delta.scale(step / distance));
        float yaw = (float) (Math.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
        return new OrphanDepartureStep(false, next, yaw);
    }
}

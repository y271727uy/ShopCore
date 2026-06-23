package com.y271727uy.shopcore.core.consumer.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public final class EntityConsumerSetup {
    private EntityConsumerSetup() {
    }

    public static <M extends Mob> M spawnScriptControlled(ServerLevel level, M mob, Vec3 position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(mob, "mob");
        Objects.requireNonNull(position, "position");

        prepareScriptControlled(mob, position);
        mob.finalizeSpawn(
                (ServerLevelAccessor) level,
                level.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.MOB_SUMMONED,
                (SpawnGroupData) null,
                null
        );
        level.addFreshEntity(mob);
        return mob;
    }

    public static <M extends Mob> M prepareScriptControlled(M mob, Vec3 position) {
        Objects.requireNonNull(mob, "mob");
        Objects.requireNonNull(position, "position");
        mob.moveTo(position);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setDeltaMovement(0.0D, 0.0D, 0.0D);
        return mob;
    }
}

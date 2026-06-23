package com.y271727uy.shopcore.core.consumer.minecraft;

import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerSetup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public final class MinecraftVillagerConsumers {
    private MinecraftVillagerConsumers() {
    }

    public static Optional<MinecraftVillagerConsumerActor> spawn(ServerLevel level, Vec3 position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            return Optional.empty();
        }
        EntityConsumerSetup.spawnScriptControlled(level, villager, position);
        return Optional.of(wrap(villager));
    }

    public static MinecraftVillagerConsumerActor wrap(Villager villager) {
        return new MinecraftVillagerConsumerActor(Objects.requireNonNull(villager, "villager"));
    }
}

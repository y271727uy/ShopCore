package com.y271727uy.shopcore.core.consumer.eazy_npc.player;

import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerSetup;
import de.markusbordihn.easynpc.entity.ModEntityType;
import de.markusbordihn.easynpc.entity.easynpc.npc.Humanoid;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public final class EazyNpcHumanoidConsumers {
    private EazyNpcHumanoidConsumers() {
    }

    public static Optional<EazyNpcConsumerActor> spawn(ServerLevel level, Vec3 position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        EntityType<Humanoid> entityType = ModEntityType.HUMANOID.get();
        Humanoid humanoid = entityType.create(level);
        if (humanoid == null) {
            return Optional.empty();
        }
        EntityConsumerSetup.spawnScriptControlled(level, humanoid, position);
        return Optional.of(wrap(humanoid));
    }

    public static EazyNpcConsumerActor wrap(Humanoid humanoid) {
        return new EazyNpcConsumerActor(Objects.requireNonNull(humanoid, "humanoid"));
    }
}

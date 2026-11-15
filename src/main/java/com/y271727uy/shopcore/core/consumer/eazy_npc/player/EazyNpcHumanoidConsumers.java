package com.y271727uy.shopcore.core.consumer.eazy_npc.player;

import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerSetup;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerPopulation;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityPersistentConsumerMemory;
import de.markusbordihn.easynpc.entity.ModEntityType;
import de.markusbordihn.easynpc.entity.easynpc.npc.Humanoid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public final class EazyNpcHumanoidConsumers {
    private EazyNpcHumanoidConsumers() {
    }

    public static EntityConsumerPopulation<EazyNpcConsumerActor> population(
            ServerLevel level, BlockPos shopPos, EntityPersistentConsumerMemory memory) {
        return new EntityConsumerPopulation<>(level, shopPos, memory, Humanoid.class,
                EazyNpcHumanoidConsumers::wrap, EazyNpcHumanoidConsumers::spawn);
    }

    public static Optional<EazyNpcConsumerActor> spawn(ServerLevel level, Vec3 position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        EntityType<Humanoid> entityType = ModEntityType.HUMANOID.get();
        Humanoid humanoid = entityType.spawn(
                level,
                ItemStack.EMPTY,
                null,
                BlockPos.containing(position),
                MobSpawnType.MOB_SUMMONED,
                true,
                false
        );
        if (humanoid == null) {
            return Optional.empty();
        }
        EntityConsumerSetup.prepareScriptControlled(humanoid, position);
        Entity entity = humanoid.getEntity();
        if (entity == null || entity.isRemoved() || !entity.isAlive()) {
            humanoid.discard();
            return Optional.empty();
        }
        return Optional.of(wrap(humanoid));
    }

    public static EazyNpcConsumerActor wrap(Humanoid humanoid) {
        return new EazyNpcConsumerActor(Objects.requireNonNull(humanoid, "humanoid"));
    }
}

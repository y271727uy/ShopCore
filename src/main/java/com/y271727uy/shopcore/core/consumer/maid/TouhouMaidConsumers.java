package com.y271727uy.shopcore.core.consumer.maid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerSetup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public final class TouhouMaidConsumers {
    private TouhouMaidConsumers() {
    }

    public static Optional<TouhouMaidConsumerActor> spawn(ServerLevel level, Vec3 position) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(position, "position");
        EntityType<EntityMaid> entityType = InitEntities.MAID.get();
        EntityMaid maid = entityType.create(level);
        if (maid == null) {
            return Optional.empty();
        }
        EntityConsumerSetup.spawnScriptControlled(level, maid, position);
        return Optional.of(wrap(maid));
    }

    public static TouhouMaidConsumerActor wrap(EntityMaid maid) {
        return new TouhouMaidConsumerActor(Objects.requireNonNull(maid, "maid"));
    }
}

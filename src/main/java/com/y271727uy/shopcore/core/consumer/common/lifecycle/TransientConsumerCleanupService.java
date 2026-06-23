package com.y271727uy.shopcore.core.consumer.common.lifecycle;

import com.y271727uy.shopcore.core.consumer.common.entity.EntityPersistentConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.order.prompt.overhead.CustomerOverheadOrderPromptService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class TransientConsumerCleanupService {
    private TransientConsumerCleanupService() {
    }

    public static int discardForShop(
            ServerLevel level,
            BlockPos shopPos,
            List<UUID> knownConsumerIds,
            EntityPersistentConsumerMemory memory,
            Predicate<Entity> candidatePredicate,
            double searchRadius
    ) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(shopPos, "shopPos");
        Objects.requireNonNull(knownConsumerIds, "knownConsumerIds");
        Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(candidatePredicate, "candidatePredicate");
        if (!Double.isFinite(searchRadius) || searchRadius < 0.0D) {
            throw new IllegalArgumentException("searchRadius must be finite and non-negative");
        }

        Set<UUID> resetIds = new HashSet<>(knownConsumerIds);
        AABB searchArea = new AABB(shopPos).inflate(searchRadius);
        for (Entity entity : level.getEntities((Entity) null, searchArea, candidatePredicate)) {
            Optional<BlockPos> boundShopPos = memory.shopPos(new EntityConsumerActor(entity));
            if (boundShopPos.isPresent() && boundShopPos.get().equals(shopPos)) {
                resetIds.add(entity.getUUID());
            }
        }

        int discarded = 0;
        for (UUID consumerId : resetIds) {
            Entity entity = level.getEntity(consumerId);
            if (entity == null) {
                continue;
            }
            CustomerOverheadOrderPromptService.clear(entity);
            entity.discard();
            discarded++;
        }
        return discarded;
    }
}

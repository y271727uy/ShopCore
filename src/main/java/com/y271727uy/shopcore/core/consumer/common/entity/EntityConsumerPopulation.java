package com.y271727uy.shopcore.core.consumer.common.entity;

import com.y271727uy.shopcore.core.consumer.common.lifecycle.TransientConsumerCleanupService;
import com.y271727uy.shopcore.core.consumer.common.runtime.ConsumerPopulation;
import com.y271727uy.shopcore.core.consumer.common.runtime.ConsumerPresence;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/** Per-call world access; the runtime never retains a level or a live entity across unloads. */
public final class EntityConsumerPopulation<A extends EntityBackedConsumerActor> implements ConsumerPopulation<A> {
    private final ServerLevel level;
    private final BlockPos shopPos;
    private final EntityPersistentConsumerMemory memory;
    private final Predicate<Entity> accepts;
    private final Function<Entity, A> wrap;
    private final BiFunction<ServerLevel, Vec3, Optional<A>> spawn;

    public <E extends Entity> EntityConsumerPopulation(ServerLevel level, BlockPos shopPos,
            EntityPersistentConsumerMemory memory, Class<E> entityClass, Function<E, A> wrap,
            BiFunction<ServerLevel, Vec3, Optional<A>> spawn) {
        this.level = Objects.requireNonNull(level, "level");
        this.shopPos = Objects.requireNonNull(shopPos, "shopPos").immutable();
        this.memory = Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(entityClass, "entityClass");
        Objects.requireNonNull(wrap, "wrap");
        this.accepts = entityClass::isInstance;
        this.wrap = entity -> wrap.apply(entityClass.cast(entity));
        this.spawn = Objects.requireNonNull(spawn, "spawn");
    }

    @Override
    public Optional<A> resolve(UUID id) {
        Entity entity = level.getEntity(id);
        return accepts.test(entity) && entity.isAlive() && !entity.isRemoved()
                ? Optional.of(wrap.apply(entity)) : Optional.empty();
    }

    @Override
    public ConsumerPresence presence(UUID id) {
        Entity entity = level.getEntity(id);
        if (entity == null) {
            return ConsumerPresence.UNLOADED;
        }
        if (!accepts.test(entity)) {
            return ConsumerPresence.REMOVED;
        }
        if (entity.isRemoved()) {
            return entity.getRemovalReason() != null && entity.getRemovalReason().shouldDestroy()
                    ? ConsumerPresence.REMOVED : ConsumerPresence.UNLOADED;
        }
        return entity.isAlive() ? ConsumerPresence.LOADED : ConsumerPresence.REMOVED;
    }

    @Override
    public Optional<A> spawn(Vec3 position) {
        return spawn.apply(level, position);
    }

    @Override
    public void bindToShop(A actor) {
        memory.setShopPos(actor, shopPos);
    }

    @Override
    public void discardForShop(List<UUID> knownIds) {
        TransientConsumerCleanupService.discardForShop(level, shopPos, knownIds, memory, accepts, 32.0D);
    }

    @Override
    public void discardForShop(List<UUID> knownIds, java.util.function.Consumer<A> beforeDiscard) {
        TransientConsumerCleanupService.discardForShop(level, shopPos, knownIds, memory, accepts, 32.0D,
                entity -> beforeDiscard.accept(wrap.apply(entity)));
    }

    @Override
    public void detach(A actor) {
        memory.setOrphanDeparture(actor, true);
        memory.clearShopPos(actor);
        memory.clearOrderId(actor);
    }
}

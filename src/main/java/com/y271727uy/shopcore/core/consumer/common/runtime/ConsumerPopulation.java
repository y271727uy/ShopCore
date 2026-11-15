package com.y271727uy.shopcore.core.consumer.common.runtime;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** World/entity access for one shop. Implementations resolve only living, loaded consumers. */
public interface ConsumerPopulation<A extends ConsumerActor> {
    Optional<A> resolve(UUID id);

    /** Return REMOVED only with positive evidence; unresolved UUIDs may be in unloaded chunks. */
    default ConsumerPresence presence(UUID id) {
        return resolve(id).isPresent() ? ConsumerPresence.LOADED : ConsumerPresence.UNLOADED;
    }

    Optional<A> spawn(Vec3 position);

    void bindToShop(A actor);

    void discardForShop(List<UUID> knownIds);

    /** Clear configured presentation/state before removing consumers. */
    default void discardForShop(List<UUID> knownIds, java.util.function.Consumer<A> beforeDiscard) {
        knownIds.forEach(id -> resolve(id).ifPresent(beforeDiscard));
        discardForShop(knownIds);
    }

    /** Transfer departure ownership away from the removed shop. */
    void detach(A actor);
}

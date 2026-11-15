package com.y271727uy.shopcore.core.consumer.common.runtime;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;

import java.util.List;

/** Optional presentation/interaction hooks, invoked in the original test-shop tick order. */
public interface ConsumerLifecycle<A extends ConsumerActor> {
    default void arrived(A actor) { }

    default void departing(A actor) { }

    default void clearPrompt(A actor) { }

    default void tickWaiting(List<A> actors, ConsumerMemory memory, long gameTime) { }

    default void clearShopPrompts() { }
}

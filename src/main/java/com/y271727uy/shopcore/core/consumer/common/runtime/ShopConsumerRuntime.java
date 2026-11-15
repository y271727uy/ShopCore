package com.y271727uy.shopcore.core.consumer.common.runtime;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.ConsumerNavigator;
import com.y271727uy.shopcore.core.consumer.common.movement.ConsumerMovement;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * One runtime per shop. Owns the consumer roster and coordinates orders, movement and cleanup.
 * Call after the shop/order tick; order cancellation and settlement remain with the shop runtime.
 * All methods are server-thread confined (except client-side NBT loading).
 */
public final class ShopConsumerRuntime<A extends ConsumerActor> {
    private final ConsumerMemory memory;
    private final ConsumerNavigator navigator;
    private final ConsumerLifecycle<A> lifecycle;
    // Keep order reservations even when an entity's chunk is temporarily unloaded.
    private final Map<UUID, UUID> consumerOrders = new LinkedHashMap<>();
    private final Runnable rosterChanged;
    private boolean resetAfterLoad;

    public ShopConsumerRuntime(ConsumerMemory memory, ConsumerNavigator navigator, ConsumerLifecycle<A> lifecycle) {
        this(memory, navigator, lifecycle, () -> { });
    }

    /** The callback marks the host dirty; roster changes do not require a full client sync. */
    public ShopConsumerRuntime(ConsumerMemory memory, ConsumerNavigator navigator, ConsumerLifecycle<A> lifecycle,
            Runnable rosterChanged) {
        this.memory = Objects.requireNonNull(memory, "memory");
        this.navigator = Objects.requireNonNull(navigator, "navigator");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
        this.rosterChanged = Objects.requireNonNull(rosterChanged, "rosterChanged");
    }

    public boolean ownsConsumer(UUID consumerId) {
        return consumerOrders.containsKey(consumerId);
    }

    public int size() {
        return consumerOrders.size();
    }

    public void tick(ConsumerPopulation<A> population, List<ConsumerOrder> orders, boolean shopOpen,
                     ConsumerMovement movement, long gameTime, Runnable onLoadReset) {
        Objects.requireNonNull(population, "population");
        Objects.requireNonNull(movement, "movement");
        Objects.requireNonNull(onLoadReset, "onLoadReset");
        orders = List.copyOf(Objects.requireNonNull(orders, "orders"));
        if (resetAfterLoad) {
            lifecycle.clearShopPrompts();
            population.discardForShop(List.copyOf(consumerOrders.keySet()), lifecycle::clearPrompt);
            clearRoster();
            resetAfterLoad = false;
            onLoadReset.run();
        }
        pruneRemoved(population);
        releaseInactiveUnloaded(population, orders);
        List<A> actors = resolve(population);
        spawnForOrders(population, orders, shopOpen, movement, actors);
        actors = resolve(population);
        markInactiveOrdersLeaving(actors, orders, shopOpen);
        movement.tick(new ArrayList<ConsumerActor>(actors), navigator, memory, shopOpen, gameTime);
        lifecycle.tickWaiting(actors, memory, gameTime);
        for (A actor : actors) {
            QueueConsumerPhase phase = memory.phase(actor);
            if (phase == QueueConsumerPhase.DONE) {
                lifecycle.departing(actor);
                lifecycle.clearPrompt(actor);
                navigator.discard(actor);
                consumerRemoved(actor.consumerId());
            } else if (phase == QueueConsumerPhase.LEAVING) {
                lifecycle.clearPrompt(actor);
            }
        }
        pruneRemoved(population);
    }

    private void spawnForOrders(ConsumerPopulation<A> population, List<ConsumerOrder> orders, boolean shopOpen,
                                ConsumerMovement movement, List<A> actors) {
        if (!shopOpen) {
            return;
        }
        Set<UUID> represented = consumerOrders.values().stream().filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!movement.entranceClear(new ArrayList<ConsumerActor>(actors))) {
            return;
        }
        for (ConsumerOrder order : orders) {
            if (represented.contains(order.orderId())) {
                continue;
            }
            Optional<A> spawned = population.spawn(movement.spawnPosition());
            if (spawned.isEmpty()) {
                continue;
            }
            A actor = spawned.get();
            memory.setOrderId(actor, order.orderId());
            population.bindToShop(actor);
            memory.setJoinedGameTime(actor, order.createdGameTime());
            consumerOrders.put(actor.consumerId(), order.orderId());
            rosterChanged.run();
            lifecycle.arrived(actor);
            return; // Preserve at most one successful spawn per tick.
        }
    }

    private void markInactiveOrdersLeaving(List<A> actors, List<ConsumerOrder> orders, boolean shopOpen) {
        Set<UUID> active = orders.stream().map(ConsumerOrder::orderId).collect(Collectors.toSet());
        for (A actor : actors) {
            Optional<UUID> orderId = memory.orderId(actor);
            if (orderId.isPresent() && !active.contains(orderId.get())) {
                lifecycle.clearPrompt(actor);
                if (shopOpen) {
                    memory.setPhase(actor, QueueConsumerPhase.LEAVING);
                }
            }
        }
    }

    /** Order-end notifications clear presentation; the chosen movement script still controls departure. */
    public void ordersEnded(ConsumerPopulation<A> population, Set<UUID> orderIds) {
        for (UUID id : List.copyOf(consumerOrders.keySet())) {
            if (!orderIds.contains(consumerOrders.get(id))) continue;
            Optional<A> actor = population.resolve(id);
            if (actor.isPresent()) {
                lifecycle.clearPrompt(actor.get());
            } else if (population.presence(id) != ConsumerPresence.LOADED) {
                consumerRemoved(id);
            }
        }
    }

    /** Cancel orders before calling. The movement script controls the subsequent departure order. */
    public void close(ConsumerPopulation<A> population) {
        lifecycle.clearShopPrompts();
        for (A actor : resolve(population)) {
            memory.clearDepartureTarget(actor);
            lifecycle.clearPrompt(actor);
        }
    }

    public void reopen(ConsumerPopulation<A> population) {
        lifecycle.clearShopPrompts();
        population.discardForShop(List.copyOf(consumerOrders.keySet()), lifecycle::clearPrompt);
        clearRoster();
        resetAfterLoad = false;
    }

    /** Preserves the existing five-block, navigator-defined departure on block removal. */
    public void detach(ConsumerPopulation<A> population) {
        lifecycle.clearShopPrompts();
        for (A actor : resolve(population)) {
            if (memory.departureTarget(actor).isEmpty()) {
                memory.setDepartureTarget(actor, navigator.prepareDeparture(actor, 5.0D));
            }
            memory.setPhase(actor, QueueConsumerPhase.LEAVING);
            lifecycle.clearPrompt(actor);
            population.detach(actor);
        }
        clearRoster();
        resetAfterLoad = false;
    }

    /** Called by entity removal notifications, never merely because resolve returned empty. */
    public void consumerRemoved(UUID id) {
        if (consumerOrders.containsKey(id)) {
            consumerOrders.remove(id);
            rosterChanged.run();
        }
    }

    private void pruneRemoved(ConsumerPopulation<A> population) {
        for (UUID id : List.copyOf(consumerOrders.keySet())) {
            if (population.presence(id) == ConsumerPresence.REMOVED) {
                consumerRemoved(id);
            }
        }
    }

    private void releaseInactiveUnloaded(ConsumerPopulation<A> population, List<ConsumerOrder> orders) {
        Set<UUID> active = orders.stream().map(ConsumerOrder::orderId).collect(Collectors.toSet());
        for (UUID id : List.copyOf(consumerOrders.keySet())) {
            UUID orderId = consumerOrders.get(id);
            if (orderId != null && !active.contains(orderId)
                    && population.presence(id) == ConsumerPresence.UNLOADED) {
                // No live actor can leave now. The orphan service takes over when it loads again.
                consumerRemoved(id);
            }
        }
    }

    private void clearRoster() {
        if (!consumerOrders.isEmpty()) {
            consumerOrders.clear();
            rosterChanged.run();
        }
    }

    private List<A> resolve(ConsumerPopulation<A> population) {
        List<A> actors = new ArrayList<>();
        for (UUID id : List.copyOf(consumerOrders.keySet())) {
            population.resolve(id).ifPresent(actors::add);
        }
        return actors;
    }

    /** Keeps the existing UUID list; optional order bindings are stored alongside it. */
    public void load(CompoundTag tag, String key) {
        consumerOrders.clear();
        ListTag consumers = tag.getList(key, CompoundTag.TAG_INT_ARRAY);
        for (int i = 0; i < consumers.size(); i++) {
            consumerOrders.put(NbtUtils.loadUUID(consumers.get(i)), null);
        }
        ListTag bindings = tag.getList(key + "OrderBindings", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < bindings.size(); i++) {
            CompoundTag binding = bindings.getCompound(i);
            if (binding.hasUUID("Consumer") && binding.hasUUID("Order")
                    && consumerOrders.containsKey(binding.getUUID("Consumer"))) {
                consumerOrders.put(binding.getUUID("Consumer"), binding.getUUID("Order"));
            }
        }
        resetAfterLoad = true;
    }

    public void save(CompoundTag tag, String key) {
        ListTag consumers = new ListTag();
        ListTag bindings = new ListTag();
        consumerOrders.forEach((id, orderId) -> {
            consumers.add(NbtUtils.createUUID(id));
            if (orderId != null) {
                CompoundTag binding = new CompoundTag();
                binding.putUUID("Consumer", id);
                binding.putUUID("Order", orderId);
                bindings.add(binding);
            }
        });
        tag.put(key, consumers);
        tag.put(key + "OrderBindings", bindings);
    }
}

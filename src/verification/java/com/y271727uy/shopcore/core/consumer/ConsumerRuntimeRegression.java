package com.y271727uy.shopcore.core.consumer;

import com.y271727uy.shopcore.core.consumer.common.*;
import com.y271727uy.shopcore.core.consumer.common.layout.*;
import com.y271727uy.shopcore.core.consumer.common.movement.*;
import com.y271727uy.shopcore.core.consumer.common.runtime.*;
import com.y271727uy.shopcore.core.consumer.common.wait.*;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/** Behavioral regression executable; uses fake consumers/world access, never a running game or save. */
public final class ConsumerRuntimeRegression {
    private static int checks;
    private static final Memory MEMORY = new Memory();
    private static final BlockPos ORIGIN = new BlockPos(12, 64, -9);

    public static void main(String[] args) {
        ShopLifecycleRegression.main(args);
        OrderTakingRegression.main(args);
        OrderLifecycleRegression.main(args);
        for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            horizontalTrace(facing);
            counterTrace(facing);
        }
        System.out.println("Movement traces passed: " + checks + " checks");
        ownershipRecovery();
        orphanDeparture();
        management();
        unloadAndPersistence();
        endedOrderNotifications();
        System.out.println("Consumer runtime regression passed: " + checks + " checks");
    }

    private static HorizontalQueueLayout horizontalLayout(Direction facing) {
        return new HorizontalQueueLayout(new ConsumerLayoutTransform(ORIGIN, facing),
                List.of(new ConsumerLayoutSlot(ConsumerLayoutRole.SERVICE, 0, BlockPos.ZERO)),
                List.of(new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 0, BlockPos.ZERO),
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 1, BlockPos.ZERO),
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 2, BlockPos.ZERO)),
                new ConsumerLayoutSlot(ConsumerLayoutRole.EXIT, 0, BlockPos.ZERO), facing, 1.0, 0.85);
    }

    private static List<ConsumerActor> actors(ConsumerLayoutTransform transform) {
        // Reverse IDs/order to exercise joined-time ordering and UUID tie breaking.
        return new ArrayList<>(List.of(new Actor(3, transform.toWorldPosition(new Vec3(8, 0, 0.5)), 2),
                new Actor(2, transform.toWorldPosition(new Vec3(6, 0, 0.5)), 1),
                new Actor(1, transform.toWorldPosition(new Vec3(4, 0, 0.5)), 1)));
    }

    private static void horizontalTrace(Direction facing) {
        HorizontalQueueLayout layout = horizontalLayout(facing);
        ConsumerMovement preset = ConsumerMovements.horizontalQueue(ORIGIN, facing);
        HorizontalQueue original = new HorizontalQueue();
        List<ConsumerActor> before = actors(layout.transform());
        List<ConsumerActor> after = actors(layout.transform());
        Navigator oldNav = new Navigator();
        Navigator newNav = new Navigator();
        equal(layout.spawnPosition(), preset.spawnPosition(), "spawn rotation " + facing);
        check(!preset.entranceClear(after), "spawn blocked at x=8");
        Actor threshold = new Actor(9, layout.transform().toWorldPosition(new Vec3(7, 0, 0.5)), 0);
        threshold.phase = QueueConsumerPhase.LEAVING;
        check(!preset.entranceClear(List.of(threshold)), "leaving consumer still blocks entrance at x=7");
        threshold.position = layout.transform().toWorldPosition(new Vec3(6.99, 0, 0.5));
        check(preset.entranceClear(List.of(threshold)), "entrance clears below x=7");
        for (int tick = 0; tick < 800; tick++) {
            transition(before, tick);
            transition(after, tick);
            boolean open = tick < 180;
            original.tick(new HorizontalQueueContext(layout, before, oldNav, MEMORY, open, tick));
            preset.tick(after, newNav, MEMORY, open, tick);
            compare(before, after, oldNav, newNav, facing + " horizontal tick " + tick);
            if (!open) {
                check(after.stream().filter(a -> MEMORY.phase(a) == QueueConsumerPhase.LEAVING).count() <= 1,
                        "close evacuates one consumer at a time");
            }
        }
        check(after.stream().allMatch(a -> MEMORY.phase(a) == QueueConsumerPhase.DONE), "all consumers leave");
    }

    private static void counterTrace(Direction facing) {
        ConsumerLayoutTransform transform = new ConsumerLayoutTransform(ORIGIN, facing);
        QueueConsumerLayout layout = new QueueConsumerLayout(transform,
                new ConsumerLayoutSlot(ConsumerLayoutRole.SERVICE, 0, BlockPos.ZERO),
                List.of(new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 0, new BlockPos(2, 0, 0))),
                new ConsumerLayoutSlot(ConsumerLayoutRole.EXIT, 0, new BlockPos(-5, 0, 0)), facing, 1.0, 0.85);
        Vec3 spawn = transform.toWorldPosition(new Vec3(8, 0, 0.5));
        ConsumerMovement preset = ConsumerMovements.counterQueue(layout, spawn, List::isEmpty);
        QueueConsumerBehavior original = new QueueConsumerBehavior();
        List<ConsumerActor> before = actors(transform);
        List<ConsumerActor> after = actors(transform);
        Navigator oldNav = new Navigator();
        Navigator newNav = new Navigator();
        check(preset.entranceClear(List.of()) && !preset.entranceClear(after), "custom entrance policy");
        equal(spawn, preset.spawnPosition(), "custom spawn");
        for (int tick = 0; tick < 300; tick++) {
            transition(before, tick);
            transition(after, tick);
            original.tick(new QueueConsumerContext(layout, before, oldNav, MEMORY, tick < 180, tick));
            preset.tick(after, newNav, MEMORY, tick < 180, tick);
            compare(before, after, oldNav, newNav, facing + " counter tick " + tick);
        }
    }

    private static void transition(List<ConsumerActor> actors, int tick) {
        Actor front = (Actor) actors.get(2);
        if (tick == 90) front.phase = QueueConsumerPhase.WAITING_FOR_DELIVERY;
        if (tick == 130) front.phase = QueueConsumerPhase.LEAVING;
        if (tick == 180) {
            // Exercise the existing close-time collapse of multiple departing consumers.
            for (ConsumerActor actor : actors) {
                Actor a = (Actor) actor;
                if (a.phase != QueueConsumerPhase.DONE) a.phase = QueueConsumerPhase.LEAVING;
            }
        }
    }

    private static void compare(List<ConsumerActor> before, List<ConsumerActor> after,
                                Navigator oldNav, Navigator newNav, String label) {
        for (int i = 0; i < before.size(); i++) {
            Actor a = (Actor) before.get(i), b = (Actor) after.get(i);
            equal(a.position, b.position, label + " position");
            equal(a.phase, b.phase, label + " phase");
            equal(a.departure, b.departure, label + " departure");
            equal(a.joined, b.joined, label + " ordering");
        }
        equal(oldNav.calls, newNav.calls, label + " navigation calls");
        oldNav.calls.clear();
        newNav.calls.clear();
    }

    private static void ownershipRecovery() {
        UUID id = new UUID(0, 42);
        com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget original = id::equals;
        com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget replacement = candidate -> false;
        check(!com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget
                .shouldDetach(false, null, id), "unloaded shop is deferred, not mistaken for removal");
        check(!com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget
                .shouldDetach(true, original, id), "valid loaded owner preserves consumer behavior");
        check(com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget
                .shouldDetach(true, null, id), "removed shop triggers orphan recovery");
        check(com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget
                .shouldDetach(true, replacement, id), "replacement shop cannot inherit old consumers");
        check(com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget
                .shouldDetach(true, original, new UUID(0, 43)), "stale consumer absent from roster is recovered");
    }

    private static void orphanDeparture() {
        for (Vec3 target : List.of(new Vec3(5, 0, 0), new Vec3(-5, 0, 0),
                new Vec3(0, 0, 5), new Vec3(0, 0, -5), new Vec3(3, 2, -4))) {
            Vec3 position = Vec3.ZERO;
            boolean finished = false;
            for (int tick = 0; tick < 100; tick++) {
                var step = com.y271727uy.shopcore.core.consumer.common.lifecycle.OrphanDepartureStep.towards(position, target);
                Vec3 delta = target.subtract(position);
                double distance = delta.length();
                equal(distance <= 0.12D, step.finished(), "original orphan arrival threshold");
                if (step.finished()) { finished = true; break; }
                equal(position.add(delta.scale(Math.min(distance, 0.1D) / distance)), step.position(),
                        "original orphan movement trajectory");
                equal((float) (Math.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F,
                        step.yaw(), "original orphan rotation");
                position = step.position();
            }
            check(finished, "detached consumer eventually leaves without shop ticks");
        }
        check(com.y271727uy.shopcore.core.consumer.common.lifecycle.OrphanDepartureStep
                .towards(Vec3.ZERO, new Vec3(Double.NaN, 0, 0)).finished(), "invalid target cannot strand consumer");
        check(com.y271727uy.shopcore.core.consumer.common.lifecycle.OrphanDepartureStep
                .towards(Vec3.ZERO, Vec3.ZERO).finished(), "zero distance avoids division by zero");
    }

    private static void management() {
        ConsumerOrder first = new ConsumerOrder(new UUID(0, 101), 11), second = new ConsumerOrder(new UUID(0, 102), 12);
        List<ConsumerOrder> book = List.of(first, second);
        Population world = new Population();
        List<String> events = new ArrayList<>();
        Navigator nav = new Navigator();
        ConsumerLifecycle<Actor> hooks = new ConsumerLifecycle<>() {
            public void arrived(Actor a) { events.add("arrive:" + a.order); }
            public void departing(Actor a) { check(a.alive, "departure effect precedes discard"); events.add("depart"); }
            public void clearPrompt(Actor a) { check(a.alive, "configured cleanup runs before discard"); events.add("clear:" + a.id); }
            public void tickWaiting(List<Actor> a, ConsumerMemory m, long time) { events.add("waiting"); }
            public void clearShopPrompts() { events.add("hud"); }
        };
        ShopConsumerRuntime<Actor> runtime = new ShopConsumerRuntime<>(MEMORY, nav, hooks);
        ConsumerMovement movement = ConsumerMovements.horizontalQueue(ORIGIN, Direction.NORTH);
        runtime.tick(world, book, true, movement, 20, () -> { throw new AssertionError("unexpected reset"); });
        check(runtime.ownsConsumer(world.living().get(0).id), "runtime exposes current ownership");
        check(runtime.size() == 1 && world.spawnAttempts == 1, "one spawn per tick");
        Actor a = world.living().get(0);
        equal(first.orderId(), a.order, "first order bound");
        check(a.bound && a.joined == 20, "shop binding and original null-phase normalization preserved");
        equal("arrive:" + first.orderId(), events.get(0), "arrival before movement and waiting");
        runtime.tick(world, book, true, movement, 21, () -> { });
        check(world.spawnAttempts == 1, "occupied entrance suppresses spawn");
        a.position = new Vec3(ORIGIN.getX() + 6.9, ORIGIN.getY(), ORIGIN.getZ() + 0.5);
        runtime.tick(world, book, true, movement, 22, () -> { });
        check(runtime.size() == 2 && world.spawnAttempts == 2, "next unrepresented order spawns");
        Actor b = world.living().get(1);
        equal(second.orderId(), b.order, "second order bound");
        b.position = new Vec3(ORIGIN.getX() + 6, ORIGIN.getY(), ORIGIN.getZ() + 0.5);
        runtime.tick(world, book, true, movement, 23, () -> { });
        check(world.spawnAttempts == 2, "represented orders never spawn twice");
        events.clear();
        List<ConsumerOrder> completed = List.of(second); // first order is no longer active
        runtime.tick(world, completed, true, movement, 24, () -> { });
        equal(QueueConsumerPhase.LEAVING, a.phase, "completed order starts departure");
        check(events.get(0).startsWith("clear:"), "inactive order prompt cleared before movement");
        a.position = a.departure;
        events.clear();
        runtime.tick(world, completed, true, movement, 25, () -> { });
        check(!a.alive && runtime.size() == 1, "arrival at exit discards and prunes in same tick");
        check(events.indexOf("waiting") < events.indexOf("depart"), "waiting update precedes departure effects");

        b.departure = new Vec3(99, 64, 99);
        events.clear();
        runtime.close(world);
        check(b.departure == null && events.get(0).equals("hud"), "close clears target and HUD");
        // Closed shop delegates departure sequencing to the selected script, with no new spawns.
        runtime.tick(world, List.of(), false, movement, 26, () -> { });
        check(world.spawnAttempts == 2 && b.phase == QueueConsumerPhase.LEAVING, "closed shop departure");
        CompoundTag saved = new CompoundTag();
        runtime.save(saved, "TestConsumers");
        ListTag ids = saved.getList("TestConsumers", CompoundTag.TAG_INT_ARRAY);
        check(ids.size() == 1, "same NBT list schema");
        equal(b.id, NbtUtils.loadUUID(ids.get(0)), "UUID round trip");
        Vec3 existingTarget = b.departure;
        runtime.detach(world);
        equal(existingTarget, b.departure, "removal preserves already chosen departure target");
        check(!runtime.ownsConsumer(b.id), "detached consumer loses ownership");
        check(b.detached && runtime.size() == 0, "removal transfers ownership and releases roster");
        events.clear();
        runtime.detach(world);
        check(events.equals(List.of("hud")), "repeated detach cannot reprocess consumers");
        // Separate shop roster exercises departure without a previously chosen target.
        ShopConsumerRuntime<Actor> freshRemoval = new ShopConsumerRuntime<>(MEMORY, nav, hooks);
        freshRemoval.load(saved, "TestConsumers");
        b.departure = null;
        freshRemoval.detach(world);
        equal(b.position.add(5, 0, 0), b.departure, "removal uses navigator's five-block departure");
        runtime.load(saved, "TestConsumers");
        events.clear();
        runtime.reopen(world);
        check(runtime.size() == 0 && !b.alive && world.discards == 1, "reopen resets old consumers");
        check(events.contains("clear:" + b.id), "reopen invokes configured cleanup");
        b.alive = true;
        events.clear();
        runtime.load(saved, "TestConsumers");
        check(runtime.size() == 1, "load preserves roster until first server tick");
        int[] resets = {0};
        runtime.tick(world, book, true, movement, 30, () -> resets[0]++);
        check(!b.alive && world.discards == 2 && resets[0] == 1 && runtime.size() == 1,
                "load cleanup occurs before respawn and marks dirty once");
        check(events.contains("clear:" + b.id), "load reset invokes configured cleanup");
        runtime.tick(world, book, true, movement, 31, () -> resets[0]++);
        check(resets[0] == 1 && world.discards == 2, "load reset is one-shot");
        Actor current = world.living().get(0);
        current.alive = false;
        runtime.tick(world, book, false, movement, 32, () -> { });
        check(runtime.size() == 0, "missing/dead consumer pruned");

        Population retryWorld = new Population();
        retryWorld.failNextSpawn = true;
        ShopConsumerRuntime<Actor> retry = new ShopConsumerRuntime<>(MEMORY, new Navigator(), new ConsumerLifecycle<>() { });
        retry.tick(retryWorld, book, true, movement, 40, () -> { });
        check(retryWorld.spawnAttempts == 2 && retry.size() == 1, "failed spawn still tries subsequent order");
        equal(second.orderId(), retryWorld.living().get(0).order, "failed first spawn doesn't bind wrong order");
        System.out.println("Management, ordering, close/reopen, removal, NBT and load-reset checks passed");
    }

    private static void unloadAndPersistence() {
        Population world = new Population();
        Navigator nav = new Navigator();
        int[] dirty = {0};
        ShopConsumerRuntime<Actor> runtime = new ShopConsumerRuntime<>(MEMORY, nav,
                new ConsumerLifecycle<>() { }, () -> dirty[0]++);
        ConsumerMovement movement = ConsumerMovements.horizontalQueue(ORIGIN, Direction.NORTH);
        List<ConsumerOrder> orders = List.of(new ConsumerOrder(new UUID(0, 201), 0));
        runtime.tick(world, orders, true, movement, 0, () -> { });
        Actor first = world.living().get(0);
        check(dirty[0] == 1, "spawn marks roster dirty");
        runtime.tick(world, orders, true, movement, 1, () -> { });
        check(dirty[0] == 1, "movement without roster change does not mark roster dirty");
        nav.calls.clear();
        first.loaded = false;
        Vec3 beforeUnload = first.position;
        for (int tick = 2; tick < 20; tick++) {
            runtime.tick(world, orders, true, movement, tick, () -> { });
        }
        check(runtime.ownsConsumer(first.id) && runtime.size() == 1, "unloaded consumer retains ownership");
        check(world.spawnAttempts == 1, "unloaded order reservation prevents duplicate consumers");
        check(dirty[0] == 1 && nav.calls.isEmpty(), "unloaded consumer neither moves nor dirties roster");
        equal(beforeUnload, first.position, "unloading does not alter position");

        CompoundTag saved = new CompoundTag();
        runtime.save(saved, "TestConsumers");
        check(saved.getList("TestConsumers", CompoundTag.TAG_INT_ARRAY).size() == 1,
                "unloaded consumer persists in original UUID list");
        ListTag bindings = saved.getList("TestConsumersOrderBindings", CompoundTag.TAG_COMPOUND);
        equal(orders.get(0).orderId(), bindings.getCompound(0).getUUID("Order"), "unloaded order binding saved");
        ShopConsumerRuntime<Actor> reloaded = new ShopConsumerRuntime<>(MEMORY, new Navigator(),
                new ConsumerLifecycle<>() { }, () -> { throw new AssertionError("reading NBT must not mark dirty"); });
        reloaded.load(saved, "TestConsumers");
        CompoundTag roundTrip = new CompoundTag();
        reloaded.save(roundTrip, "TestConsumers");
        equal(saved, roundTrip, "roster and order bindings round trip without live entities");

        first.loaded = true;
        runtime.tick(world, orders, true, movement, 20, () -> { });
        check(world.spawnAttempts == 1 && runtime.ownsConsumer(first.id), "returning consumer resumes same ownership");
        check(!first.position.equals(beforeUnload), "returning consumer resumes movement");
        check(dirty[0] == 1, "returning to loaded state does not rewrite roster");
        world.actors.remove(first.id); // Entity has already left the level when its removal event arrives.
        runtime.consumerRemoved(first.id);
        check(!runtime.ownsConsumer(first.id) && dirty[0] == 2, "confirmed removal releases reservation and marks dirty");
        runtime.consumerRemoved(first.id);
        runtime.consumerRemoved(new UUID(0, 999));
        check(dirty[0] == 2, "repeated or unknown removal notification is harmless");
        runtime.tick(world, orders, true, movement, 21, () -> { });
        check(world.spawnAttempts == 2 && runtime.size() == 1 && dirty[0] == 3,
                "confirmed removal allows a single replacement for still-active order");

        Actor second = world.living().get(0);
        second.loaded = false;
        runtime.tick(world, List.of(), false, movement, 22, () -> { });
        check(!runtime.ownsConsumer(second.id) && dirty[0] == 4,
                "finished order releases unloaded roster so returning consumer can be recovered as orphan");
        runtime.tick(world, List.of(), false, movement, 23, () -> { });
        check(dirty[0] == 4, "inactive unloaded cleanup is one-shot");
        runtime.tick(world, orders, true, movement, 24, () -> { });
        Actor third = world.living().get(1);
        third.alive = false;
        runtime.tick(world, List.of(), false, movement, 25, () -> { });
        check(runtime.size() == 0 && dirty[0] == 6, "observed dead entity also releases roster and marks dirty");

        // Reset-on-load remains the chosen behavior, including old saves without order-binding metadata.
        CompoundTag legacy = saved.copy();
        legacy.remove("TestConsumersOrderBindings");
        Population loadedWorld = new Population();
        int[] resetDirty = {0}, resetCalls = {0};
        ShopConsumerRuntime<Actor> legacyRuntime = new ShopConsumerRuntime<>(MEMORY, new Navigator(),
                new ConsumerLifecycle<>() { }, () -> resetDirty[0]++);
        legacyRuntime.load(legacy, "TestConsumers");
        check(legacyRuntime.ownsConsumer(first.id) && resetDirty[0] == 0, "legacy roster loads without side effects");
        legacyRuntime.tick(loadedWorld, orders, true, movement, 30, () -> resetCalls[0]++);
        check(loadedWorld.discards == 1 && loadedWorld.spawnAttempts == 1 && resetCalls[0] == 1,
                "legacy save still resets before generating customers");
        check(resetDirty[0] == 2, "load reset and replacement spawn both notify persistence");
        legacyRuntime.tick(loadedWorld, orders, true, movement, 31, () -> resetCalls[0]++);
        check(resetCalls[0] == 1 && resetDirty[0] == 2, "reset and dirty notifications are not repeated");
        legacyRuntime.reopen(loadedWorld);
        check(legacyRuntime.size() == 0 && resetDirty[0] == 3, "reopen marks roster dirty");
        legacyRuntime.reopen(loadedWorld);
        check(resetDirty[0] == 3, "empty reopen does not rewrite roster");
        legacyRuntime.tick(loadedWorld, orders, true, movement, 32, () -> resetCalls[0]++);
        legacyRuntime.detach(loadedWorld);
        check(legacyRuntime.size() == 0 && resetDirty[0] == 5, "detach releases ownership and marks dirty");
        legacyRuntime.detach(loadedWorld);
        check(resetDirty[0] == 5, "repeated detach does not rewrite roster");
        System.out.println("Unload, order reservations, removal confirmation, dirty notifications and legacy saves passed");
    }

    private static void endedOrderNotifications() {
        Population world = new Population();
        List<UUID> cleared = new ArrayList<>();
        int[] dirty = {0};
        ShopConsumerRuntime<Actor> runtime = new ShopConsumerRuntime<>(MEMORY, new Navigator(),
                new ConsumerLifecycle<>() {
                    public void clearPrompt(Actor actor) { cleared.add(actor.id); }
                }, () -> dirty[0]++);
        ConsumerMovement movement = ConsumerMovements.horizontalQueue(ORIGIN, Direction.NORTH);
        UUID firstOrder = new UUID(0, 801), secondOrder = new UUID(0, 802);
        List<ConsumerOrder> orders = List.of(new ConsumerOrder(firstOrder, 0), new ConsumerOrder(secondOrder, 1));
        runtime.tick(world, orders, true, movement, 0, () -> { });
        Actor first = world.living().get(0);
        first.position = new Vec3(ORIGIN.getX() + 6, ORIGIN.getY(), ORIGIN.getZ() + 0.5);
        runtime.tick(world, orders, true, movement, 1, () -> { });
        Actor second = world.living().get(1);
        QueueConsumerPhase originalPhase = first.phase;
        Vec3 originalPosition = first.position;
        runtime.ordersEnded(world, Set.of(firstOrder));
        equal(List.of(first.id), cleared, "ended order clears only its own consumer prompt");
        equal(originalPhase, first.phase, "notification does not override chosen movement script");
        equal(originalPosition, first.position, "notification does not move consumer");
        check(runtime.ownsConsumer(first.id), "loaded consumer remains owned until actual departure");
        second.loaded = false;
        int before = dirty[0];
        runtime.ordersEnded(world, Set.of(secondOrder));
        check(!runtime.ownsConsumer(second.id) && dirty[0] == before + 1,
                "unloaded terminal consumer is handed to orphan recovery and persisted");
        runtime.ordersEnded(world, Set.of(secondOrder));
        check(dirty[0] == before + 1, "repeated terminal notification cannot release twice");
        check(runtime.ownsConsumer(first.id), "ending another order preserves existing departure");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void equal(Object expected, Object actual, String message) {
        check(Objects.equals(expected, actual), message + ": expected=" + expected + ", actual=" + actual);
    }

    private static final class Actor implements ConsumerActor {
        final UUID id;
        Vec3 position;
        boolean alive = true, loaded = true, bound, detached;
        QueueConsumerPhase phase;
        long joined;
        UUID order;
        Vec3 departure;
        Actor(long id, Vec3 position, long joined) { this.id = new UUID(0, id); this.position = position; this.joined = joined; }
        public UUID consumerId() { return id; }
        public Vec3 position() { return position; }
        public boolean isAlive() { return alive; }
    }

    private static final class Memory implements ConsumerMemory {
        public QueueConsumerPhase phase(ConsumerActor a) { return ((Actor) a).phase; }
        public void setPhase(ConsumerActor a, QueueConsumerPhase p) { ((Actor) a).phase = p; }
        public long joinedGameTime(ConsumerActor a) { return ((Actor) a).joined; }
        public void setJoinedGameTime(ConsumerActor a, long t) { ((Actor) a).joined = t; }
        public Optional<UUID> orderId(ConsumerActor a) { return Optional.ofNullable(((Actor) a).order); }
        public void setOrderId(ConsumerActor a, UUID id) { ((Actor) a).order = id; }
        public void clearOrderId(ConsumerActor a) { ((Actor) a).order = null; }
        public Optional<Vec3> departureTarget(ConsumerActor a) { return Optional.ofNullable(((Actor) a).departure); }
        public void setDepartureTarget(ConsumerActor a, Vec3 p) { ((Actor) a).departure = p; }
        public void clearDepartureTarget(ConsumerActor a) { ((Actor) a).departure = null; }
    }

    private static final class Navigator implements ConsumerNavigator {
        final List<String> calls = new ArrayList<>();
        public void moveTowards(ConsumerActor actor, Vec3 target, double speed) {
            calls.add("move:" + actor.consumerId() + ":" + target + ":" + speed);
            Actor a = (Actor) actor;
            Vec3 delta = target.subtract(a.position);
            double distance = delta.length();
            if (distance > 1.0E-4) a.position = a.position.add(delta.normalize().scale(Math.min(distance, 0.1 * Math.max(0.1, speed))));
        }
        public void stop(ConsumerActor a) { calls.add("stop:" + a.consumerId()); }
        public void face(ConsumerActor a, Direction d) { calls.add("face:" + a.consumerId() + ":" + d); }
        public void discard(ConsumerActor a) { ((Actor) a).alive = false; calls.add("discard:" + a.consumerId()); }
        public Vec3 prepareDeparture(ConsumerActor a, double distance) { return a.position().add(distance, 0, 0); }
    }

    private static final class Population implements ConsumerPopulation<Actor> {
        final Map<UUID, Actor> actors = new LinkedHashMap<>();
        int spawnAttempts, discards;
        boolean failNextSpawn;
        public Optional<Actor> resolve(UUID id) {
            return Optional.ofNullable(actors.get(id)).filter(a -> a.loaded && a.alive);
        }
        public ConsumerPresence presence(UUID id) {
            Actor a = actors.get(id);
            if (a == null || !a.loaded) return ConsumerPresence.UNLOADED;
            return a.alive ? ConsumerPresence.LOADED : ConsumerPresence.REMOVED;
        }
        public Optional<Actor> spawn(Vec3 position) {
            spawnAttempts++;
            if (failNextSpawn) { failNextSpawn = false; return Optional.empty(); }
            Actor a = new Actor(spawnAttempts, position, 0);
            actors.put(a.id, a);
            return Optional.of(a);
        }
        public void bindToShop(Actor a) { a.bound = true; }
        public void discardForShop(List<UUID> ids) { discards++; actors.values().stream().filter(a -> a.bound || ids.contains(a.id)).forEach(a -> a.alive = false); }
        public void detach(Actor a) { a.detached = true; }
        List<Actor> living() { return actors.values().stream().filter(Actor::isAlive).toList(); }
    }
}


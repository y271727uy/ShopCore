package com.y271727uy.shopcore.core.consumer;
import com.y271727uy.shopcore.core.consumer.common.*;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.core.order.taking.*;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Proxy;
import java.util.*;

public final class OrderTakingRegression {
    public static void main(String[] args) {
        ConsumerActor actor = new ConsumerActor() {
            public UUID consumerId() { return new UUID(0, 1); }
            public Vec3 position() { return Vec3.ZERO; }
            public boolean isAlive() { return true; }
        };
        List<String> calls = new ArrayList<>();
        ConsumerMemory memory = (ConsumerMemory) Proxy.newProxyInstance(ConsumerMemory.class.getClassLoader(),
                new Class<?>[]{ConsumerMemory.class}, (p,m,a) -> {
                    if (m.getName().equals("setPhase")) {
                        if (a[1] != QueueConsumerPhase.WAITING_FOR_DELIVERY) throw new AssertionError();
                        calls.add("phase"); return null;
                    }
                    throw new AssertionError("Unexpected memory access: " + m);
                });
        for (long wait : new long[]{0, 1, 40}) {
            Timing timing = new Timing();
            AutomaticOrderTaking behavior = new AutomaticOrderTaking(wait, timing);
            behavior.tick(actor, memory, 100, () -> calls.add("waiting"), () -> calls.add("accepted"));
            expect(calls, "waiting");
            if (timing.time.orElseThrow() != 100 + wait) throw new AssertionError("deadline");
            for (long t=101; t<100+wait; t++) {
                behavior.tick(actor, memory, t, () -> calls.add("waiting"), () -> calls.add("accepted"));
                expect(calls,"waiting");
            }
            behavior.tick(actor,memory,Math.max(101,100+wait), () -> calls.add("waiting"), () -> calls.add("accepted"));
            expect(calls,"accepted","phase");
            if (timing.time.isPresent()) throw new AssertionError("timer not cleared");
            if (behavior.request(actor,memory, () -> calls.add("accepted"))) throw new AssertionError("automatic manual request");
            expect(calls);
            timing.time=OptionalLong.of(500); // persisted deadline is respected, not restarted
            behavior.tick(actor,memory,499, () -> calls.add("waiting"), () -> calls.add("accepted"));
            expect(calls,"waiting");
            behavior.clear(actor);
            if(timing.time.isPresent()) throw new AssertionError("close cleanup");
        }
        Timing timing = new Timing(); timing.time=OptionalLong.of(1);
        AutomaticOrderTaking behavior=new AutomaticOrderTaking(40,timing);
        try {
            behavior.tick(actor,memory,2, () -> {}, () -> { throw new IllegalStateException("display failed"); });
            throw new AssertionError("expected display failure");
        } catch (IllegalStateException expected) { }
        expect(calls);
        if(timing.time.isEmpty()) throw new AssertionError("failure lost timer");
        ManualOrderTaking manual = new ManualOrderTaking();
        manual.tick(actor,memory,9999, () -> calls.add("waiting"), () -> calls.add("accepted"));
        expect(calls,"waiting");
        if(!manual.request(actor,memory, () -> calls.add("accepted"))) throw new AssertionError();
        expect(calls,"accepted","phase");
        System.out.println("Order taking regression passed: delay boundaries, zero delay, saved timer, cleanup, display failure and manual acceptance");
    }
    private static void expect(List<String> calls,String... expected) {
        if(!calls.equals(List.of(expected))) throw new AssertionError(calls + " != " + List.of(expected));
        calls.clear();
    }
    private static final class Timing implements OrderTakingMemory {
        OptionalLong time=OptionalLong.empty();
        public OptionalLong revealTime(ConsumerActor actor) { return time; }
        public void setRevealTime(ConsumerActor actor,long t) { time=OptionalLong.of(t); }
        public void clear(ConsumerActor actor) { time=OptionalLong.empty(); }
    }
}

package com.y271727uy.shopcore.core.consumer;

import com.y271727uy.shopcore.core.shop.runtime.ShopLifecycleController;
import java.util.ArrayList;
import java.util.List;

public final class ShopLifecycleRegression {
    public static void main(String[] args) {
        List<String> events = new ArrayList<>();
        boolean[] open = {false};
        var controller = new ShopLifecycleController<String>(100, new ShopLifecycleController.Hooks<String>() {
            public void initialize() { events.add("init"); }
            public boolean openRequested() { return open[0]; }
            public void setOpenRequested(boolean value) { open[0] = value; events.add("request:" + value); }
            public void tickRuntime(String c) { events.add("runtime:" + c); }
            public void tickComponents(String c) { events.add("components:" + c); }
            public void cancelActiveOrders() { events.add("cancel"); }
            public void closeComponents(String c) { events.add("close"); }
            public void reopenComponents(String c) { events.add("reopen"); }
            public void detachComponents(String c) { events.add("detach"); }
            public void changed() { events.add("changed"); }
        });
        boolean forced = true;
        for (long time = 37; time < 450; time++) {
            if (time == 70 || time == 199) {
                controller.requestRuntimeTick(); // load/explicit refresh between world-time boundaries
                forced = true;
            }
            controller.tick("world", time);
            if (forced || time % 100 == 0) expect(events, "runtime:world", "components:world");
            else expect(events, "components:world");
            forced = false;
        }
        if (!controller.toggleOpenRequested("world")) throw new AssertionError("opening request");
        expect(events, "init", "request:true", "reopen", "changed");
        controller.tick("world", 451);
        expect(events, "runtime:world", "components:world");
        if (controller.toggleOpenRequested("world")) throw new AssertionError("closing request");
        expect(events, "init", "request:false", "cancel", "close", "changed");
        controller.tick("world", 452);
        expect(events, "runtime:world", "components:world");
        controller.removed("world");
        expect(events, "cancel", "detach");
        controller.toggleOpenRequested(null);
        expect(events, "init", "request:true", "changed");
        controller.requestRuntimeTick();
        expect(events);
        controller.tick("reloaded-world", 499);
        expect(events, "runtime:reloaded-world", "components:reloaded-world");
        controller.tick("reloaded-world", 500);
        expect(events, "runtime:reloaded-world", "components:reloaded-world");
        if (!open[0]) throw new AssertionError("load refresh changed saved request");
        System.out.println("Shop lifecycle regression passed: cadence, ordering, close/reopen, removal and reload");
    }

    private static void expect(List<String> actual, String... expected) {
        if (!actual.equals(List.of(expected))) throw new AssertionError(actual + " != " + List.of(expected));
        actual.clear();
    }
}

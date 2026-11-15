package com.y271727uy.shopcore.core.shop.runtime;

import java.util.Objects;

/** Per-shop coordinator. Context is supplied per call, never retained across world unloads. */
public final class ShopLifecycleController<C> {
    public interface Hooks<C> {
        void initialize();
        boolean openRequested();
        void setOpenRequested(boolean requested);
        void tickRuntime(C context);
        default void tickComponents(C context) { }
        void cancelActiveOrders();
        default void closeComponents(C context) { }
        default void reopenComponents(C context) { }
        default void detachComponents(C context) { }
        void changed();
    }

    private final long intervalTicks;
    private final Hooks<C> hooks;
    private boolean forceRuntimeTick = true;

    public ShopLifecycleController(long intervalTicks, Hooks<C> hooks) {
        if (intervalTicks < 1) throw new IllegalArgumentException("intervalTicks must be positive");
        this.intervalTicks = intervalTicks;
        this.hooks = Objects.requireNonNull(hooks, "hooks");
    }

    /** Server thread only. Preserve world-time alignment, including after a forced update. */
    public void tick(C context, long gameTime) {
        Objects.requireNonNull(context, "context");
        if (gameTime < 0) throw new IllegalArgumentException("gameTime cannot be negative");
        if (forceRuntimeTick || gameTime % intervalTicks == 0L) {
            forceRuntimeTick = false;
            hooks.tickRuntime(context);
        }
        hooks.tickComponents(context);
    }

    /** Null context supports the existing request-only behavior before a server level is attached. */
    public boolean toggleOpenRequested(C context) {
        hooks.initialize();
        boolean opening = !hooks.openRequested();
        hooks.setOpenRequested(opening);
        requestRuntimeTick();
        if (context != null) {
            if (opening) {
                hooks.reopenComponents(context);
            } else {
                hooks.cancelActiveOrders();
                hooks.closeComponents(context);
            }
        }
        hooks.changed();
        return opening;
    }

    public void removed(C context) {
        Objects.requireNonNull(context, "context");
        hooks.cancelActiveOrders();
        hooks.detachComponents(context);
    }

    /** Call after loading persisted components. Does not tick or change the saved open request. */
    public void requestRuntimeTick() {
        forceRuntimeTick = true;
    }
}

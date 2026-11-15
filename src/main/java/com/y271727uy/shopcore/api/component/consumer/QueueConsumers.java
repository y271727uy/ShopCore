package com.y271727uy.shopcore.api.component.consumer;

import net.minecraft.network.chat.Component;

import java.util.Objects;

/** Easy NPC humanoids, the existing horizontal queue, and delayed overhead order taking. */
public record QueueConsumers(long orderWaitTicks, Component waitingText, String storageKey) {
    public QueueConsumers {
        if (orderWaitTicks < 0) throw new IllegalArgumentException("orderWaitTicks cannot be negative");
        waitingText = Objects.requireNonNull(waitingText, "waitingText").copy();
        storageKey = Objects.requireNonNull(storageKey, "storageKey").trim();
        if (storageKey.isEmpty()) throw new IllegalArgumentException("storageKey cannot be blank");
        if (storageKey.equals("ShopRuntime") || storageKey.equals("id")
                || storageKey.equals("x") || storageKey.equals("y") || storageKey.equals("z")) {
            throw new IllegalArgumentException("storageKey conflicts with block entity storage");
        }
    }

    @Override
    public Component waitingText() {
        return waitingText.copy();
    }
}

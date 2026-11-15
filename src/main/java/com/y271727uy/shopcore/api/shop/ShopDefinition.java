package com.y271727uy.shopcore.api.shop;

import com.y271727uy.shopcore.api.component.consumer.QueueConsumers;
import com.y271727uy.shopcore.api.component.menu.MenuSource;
import com.y271727uy.shopcore.api.component.order.OrderSettings;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/** Shareable assembly settings. Mutable runtime state is created separately for each host. */
public record ShopDefinition(ResourceLocation id, MenuSource menu, QueueConsumers consumers,
                             OrderSettings orders, long runtimeIntervalTicks, int minimumListings,
                             String deliveryMessagePrefix) {
    public ShopDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(menu, "menu");
        Objects.requireNonNull(consumers, "consumers");
        Objects.requireNonNull(orders, "orders");
        if (runtimeIntervalTicks < 1) throw new IllegalArgumentException("runtimeIntervalTicks must be positive");
        if (minimumListings < 0) throw new IllegalArgumentException("minimumListings cannot be negative");
        deliveryMessagePrefix = Objects.requireNonNull(deliveryMessagePrefix, "deliveryMessagePrefix").trim();
        if (deliveryMessagePrefix.isEmpty()) throw new IllegalArgumentException("deliveryMessagePrefix cannot be blank");
    }
}

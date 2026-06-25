package com.y271727uy.shopcore.core.architecture.eventdriven;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Stable identifier for a high-frequency fact, such as an order completion or customer visit.
 */
public record EventKey(ResourceLocation id) {
    public EventKey {
        Objects.requireNonNull(id, "id");
    }

    public static EventKey of(ResourceLocation id) {
        return new EventKey(id);
    }

    public static EventKey of(String namespace, String path) {
        return new EventKey(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static EventKey parse(String id) {
        ResourceLocation location = ResourceLocation.tryParse(Objects.requireNonNull(id, "id"));
        if (location == null) {
            throw new IllegalArgumentException("Invalid event key: " + id);
        }
        return new EventKey(location);
    }
}

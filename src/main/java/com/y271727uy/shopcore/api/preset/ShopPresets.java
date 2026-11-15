package com.y271727uy.shopcore.api.preset;

import com.y271727uy.shopcore.api.component.consumer.QueueConsumers;
import com.y271727uy.shopcore.api.component.menu.MenuSource;
import com.y271727uy.shopcore.api.component.order.OrderSettings;
import com.y271727uy.shopcore.api.shop.ShopDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Preassembled shops use the same public components available to custom assemblies. */
public final class ShopPresets {
    private ShopPresets() { }

    /** Existing test-shop behavior: five-second runtime tick, two-second order taking, one-minute orders. */
    public static ShopDefinition horizontalQueue(ResourceLocation id, MenuSource menu,
                                                 String consumerStorageKey, String deliveryMessagePrefix) {
        return new ShopDefinition(id, menu,
                new QueueConsumers(40L, Component.literal("等待中..."), consumerStorageKey),
                new OrderSettings(20L, 1200L), 100L, 1, deliveryMessagePrefix);
    }
}

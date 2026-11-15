package com.y271727uy.shopcore.api.shop;

import com.y271727uy.shopcore.api.component.order.DeliveryReceipt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Optional gameplay effects. These callbacks do not own order or consumer state transitions. */
public interface ShopFeedback {
    default void arrived(Entity consumer) { }
    default void departing(Entity consumer) { }
    default void delivered(ServerPlayer player, DeliveryReceipt receipt) { }
}

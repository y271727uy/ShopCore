package com.y271727uy.shopcore.core.order.interaction;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface OrderPromptClearer {
    void clear(ServerPlayer player);
}

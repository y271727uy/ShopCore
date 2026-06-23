package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.order.prompt.OrderPrompt;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface OrderPromptSink {
    void show(ServerPlayer player, OrderPrompt prompt);
}

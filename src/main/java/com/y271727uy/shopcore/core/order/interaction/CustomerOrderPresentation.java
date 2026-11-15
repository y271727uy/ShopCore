package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.order.prompt.OrderPrompt;
import com.y271727uy.shopcore.core.order.prompt.overhead.CustomerOverheadOrderPromptService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Replaceable delivery prompt output, separate from item matching and settlement. */
public interface CustomerOrderPresentation {
    void show(ServerPlayer player, Entity customer, OrderPrompt prompt);

    void clear(ServerPlayer player, Entity customer);

    CustomerOrderPresentation OVERHEAD = new CustomerOrderPresentation() {
        @Override
        public void show(ServerPlayer player, Entity customer, OrderPrompt prompt) {
            CustomerOverheadOrderPromptService.show(customer, prompt);
        }

        @Override
        public void clear(ServerPlayer player, Entity customer) {
            CustomerOverheadOrderPromptService.clear(customer);
        }
    };
}

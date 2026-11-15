package com.y271727uy.shopcore.api.component.consumer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

/** Opts a shop into consumer interaction routing. PASS preserves the entity's normal interaction. */
public interface ConsumerInteractionTarget {
    InteractionResult interactWithConsumer(ServerPlayer player, Entity consumer, InteractionHand hand);
}

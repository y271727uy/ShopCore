package com.y271727uy.shopcore.api.shop;

import com.y271727uy.shopcore.api.component.consumer.ConsumerInteractionTarget;
import com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/** Allows integrations to find a runtime without depending on a concrete block entity implementation. */
public interface ShopRuntimeHost extends ConsumerInteractionTarget, ConsumerOwnershipTarget {
    ShopRuntime shopRuntime();

    @Override
    default boolean ownsConsumer(UUID consumerId) { return shopRuntime().ownsConsumer(consumerId); }

    @Override
    default void consumerRemoved(UUID consumerId) { shopRuntime().consumerRemoved(consumerId); }

    @Override
    default InteractionResult interactWithConsumer(ServerPlayer player, Entity consumer, InteractionHand hand) {
        player.displayClientMessage(shopRuntime().deliver(player, consumer, player.getItemInHand(hand)).message()
                .withStyle(ChatFormatting.AQUA), false);
        return InteractionResult.CONSUME;
    }
}

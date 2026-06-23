package com.y271727uy.shopcore.text;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityPersistentConsumerMemory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TestShopCustomerInteraction {
    private static final EntityPersistentConsumerMemory CONSUMER_MEMORY = new EntityPersistentConsumerMemory();

    private TestShopCustomerInteraction() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity target = event.getTarget();
        EntityConsumerActor actor = new EntityConsumerActor(target);
        Optional<BlockPos> shopPos = CONSUMER_MEMORY.shopPos(actor);
        if (shopPos.isEmpty()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(shopPos.get());
        if (!(blockEntity instanceof TestShopBlockEntity testShop)) {
            return;
        }

        ItemStack held = player.getItemInHand(event.getHand());
        player.displayClientMessage(testShop.deliverCustomerOrder(player, target, held).withStyle(ChatFormatting.AQUA), false);
        event.setCancellationResult(InteractionResult.CONSUME);
        event.setCanceled(true);
    }
}

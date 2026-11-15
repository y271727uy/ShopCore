package com.y271727uy.shopcore.event;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityPersistentConsumerMemory;
import com.y271727uy.shopcore.api.component.consumer.ConsumerInteractionTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Routes entity interaction to an opted-in shop, independent of its implementation or NPC type. */
@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ConsumerInteractionEvents {
    private static final EntityPersistentConsumerMemory MEMORY = new EntityPersistentConsumerMemory();

    private ConsumerInteractionEvents() { }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        MEMORY.shopPos(new EntityConsumerActor(event.getTarget())).ifPresent(pos -> {
            if (level.getBlockEntity(pos) instanceof ConsumerInteractionTarget target) {
                InteractionResult result = target.interactWithConsumer(player, event.getTarget(), event.getHand());
                if (result != InteractionResult.PASS) {
                    event.setCancellationResult(result);
                    event.setCanceled(true);
                }
            }
        });
    }
}

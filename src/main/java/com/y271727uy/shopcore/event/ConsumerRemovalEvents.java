package com.y271727uy.shopcore.event;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityPersistentConsumerMemory;
import com.y271727uy.shopcore.api.component.consumer.ConsumerOwnershipTarget;
import com.y271727uy.shopcore.core.order.prompt.overhead.CustomerOverheadOrderPromptService;
import com.y271727uy.shopcore.core.order.taking.EntityOrderTakingMemory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Releases reservations when destruction is confirmed, even after the entity left world lookup. */
@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ConsumerRemovalEvents {
    private static final EntityPersistentConsumerMemory MEMORY = new EntityPersistentConsumerMemory();
    private static final EntityOrderTakingMemory ORDER_TIMING = new EntityOrderTakingMemory();

    private ConsumerRemovalEvents() { }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (reason == null || (!reason.shouldDestroy() && reason != Entity.RemovalReason.CHANGED_DIMENSION)) {
            return; // Chunk unload and end-of-world saving do not end ownership.
        }
        EntityConsumerActor actor = new EntityConsumerActor(entity);
        MEMORY.shopPos(actor).ifPresent(pos -> {
            CustomerOverheadOrderPromptService.clear(entity);
            ORDER_TIMING.clear(actor);
            if (level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof ConsumerOwnershipTarget owner) {
                owner.consumerRemoved(entity.getUUID());
            }
        });
    }
}

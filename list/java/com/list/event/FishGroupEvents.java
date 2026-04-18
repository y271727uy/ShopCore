package com.list.event;

import com.list.ListMod;
import com.list.gameplay.fish_group.entity.AbstractFishPoolEntity;
import com.list.gameplay.fish_group.pool.FishPoolLootManager;
import com.list.util.FloatingPoolsSpawnerHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = ListMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FishGroupEvents {
    private static final double HOOK_INTERACTION_HORIZONTAL_RANGE = 2.5D;
    private static final double HOOK_INTERACTION_VERTICAL_RANGE = 4.5D;

    private FishGroupEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            FloatingPoolsSpawnerHelper.tick(serverLevel);
            serverLevel.players().forEach(player -> {
                if (player.fishing == null || player.fishing.isRemoved()) {
                    return;
                }
                findHookTarget(player.fishing).ifPresent(AbstractFishPoolEntity::markHookInteracting);
            });
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (event.getHookEntity() == null || event.getHookEntity().level().isClientSide()) {
            return;
        }

        var owner = event.getHookEntity().getPlayerOwner();
        if (owner == null) {
            return;
        }

        findHookTarget(event.getHookEntity()).ifPresent(entity -> {
            entity.markHookInteracting();
            entity.onFishHookInteract(owner);
        });
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(FishPoolLootManager.INSTANCE);
        ListMod.LOGGER.info("Registered fish pool loot reload listener");
    }

    private static Optional<AbstractFishPoolEntity> findHookTarget(net.minecraft.world.entity.Entity hookEntity) {
        Vec3 hookPosition = hookEntity.position();
        AABB hookBox = hookEntity.getBoundingBox()
                .inflate(HOOK_INTERACTION_HORIZONTAL_RANGE, HOOK_INTERACTION_VERTICAL_RANGE, HOOK_INTERACTION_HORIZONTAL_RANGE);

        return hookEntity.level().getEntitiesOfClass(AbstractFishPoolEntity.class, hookBox)
                .stream()
                .filter(entity -> !entity.isRemoved())
                .filter(entity -> entity.getHookInteractionBounds().intersects(hookBox))
                .min(Comparator.comparingDouble(entity -> entity.getHookInteractionBounds().distanceToSqr(hookPosition)));
    }


}



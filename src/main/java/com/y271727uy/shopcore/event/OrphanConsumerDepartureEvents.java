package com.y271727uy.shopcore.event;

import com.y271727uy.shopcore.core.consumer.common.lifecycle.OrphanConsumerDepartureService;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "shopcore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OrphanConsumerDepartureEvents {
    private OrphanConsumerDepartureEvents() { }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            OrphanConsumerDepartureService.tick(level);
        }
    }
}

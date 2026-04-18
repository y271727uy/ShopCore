package com.list.gameplay.fish_group.client;

import com.list.ListMod;
import com.list.gameplay.fish_group.client.model.entity.OceanFishPoolModel;
import com.list.gameplay.fish_group.client.model.entity.RiverFishPoolModel;
import com.list.gameplay.fish_group.client.renderer.entity.OceanFishPoolRenderer;
import com.list.gameplay.fish_group.client.renderer.entity.RiverFishPoolRenderer;
import com.list.gameplay.fish_group.item.FishGroupRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ListMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FishGroupClientEvents {
    private FishGroupClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (FishGroupRegistry.FishPoolRegistration registration : FishGroupRegistry.getFishPoolRegistrations()) {
            registerFishPoolRenderer(event, registration);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerFishPoolRenderer(EntityRenderersEvent.RegisterRenderers event, FishGroupRegistry.FishPoolRegistration registration) {
        EntityType entityType = registration.entityType().get();
        EntityRendererProvider rendererProvider = switch (registration.environment()) {
            case RIVER -> RiverFishPoolRenderer::new;
            case OCEAN -> OceanFishPoolRenderer::new;
        };
        event.registerEntityRenderer(entityType, rendererProvider);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RiverFishPoolModel.LAYER_LOCATION, RiverFishPoolModel::getTexturedModelData);
        event.registerLayerDefinition(OceanFishPoolModel.LAYER_LOCATION, OceanFishPoolModel::getTexturedModelData);
    }
}



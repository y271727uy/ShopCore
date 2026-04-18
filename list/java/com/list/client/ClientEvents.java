package com.list.client;

import com.list.ListMod;
import com.list.client.sellingbin.SellingBinClientPriceCache;
import com.list.client.sellingbin.SellingBinClientPriceHelper;
import com.list.gameplay.fish_group.item.FishGroupRegistry;
import com.list.gameplay.fish_group.client.model.entity.OceanFishPoolModel;
import com.list.gameplay.fish_group.client.model.entity.RiverFishPoolModel;
import com.list.gameplay.fish_group.client.renderer.entity.OceanFishPoolRenderer;
import com.list.gameplay.fish_group.client.renderer.entity.RiverFishPoolRenderer;
import com.list.integration.jei.tooltip.SellingBinClientTooltipComponent;
import com.list.integration.jei.tooltip.SellingBinTooltipComponent;
import com.list.item.EggItem;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = ListMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ItemProperties.register(
                    FishGroupRegistry.BAMBOO_FISHING_ROD.get(),
                    ListMod.rl("cast"),
                    (stack, level, entity, seed) -> {
                        if (!(entity instanceof Player player)) {
                            return 0.0F;
                        }

                        boolean mainHand = player.getMainHandItem() == stack;
                        boolean offHand = player.getOffhandItem() == stack;
                        if (player.getMainHandItem().getItem() instanceof FishingRodItem) {
                            offHand = false;
                        }

                        return (mainHand || offHand) && player.fishing != null ? 1.0F : 0.0F;
                    }
            ));
        }

        @SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            for (FishGroupRegistry.FishPoolRegistration registration : FishGroupRegistry.getFishPoolRegistrations()) {
                switch (registration.environment()) {
                    case RIVER -> event.registerEntityRenderer(
                            (net.minecraft.world.entity.EntityType<com.list.gameplay.fish_group.entity.RiverFishPoolEntity>) registration.entityType().get(),
                            RiverFishPoolRenderer::new
                    );
                    case OCEAN -> event.registerEntityRenderer(
                            (net.minecraft.world.entity.EntityType<com.list.gameplay.fish_group.entity.OceanFishPoolEntity>) registration.entityType().get(),
                            OceanFishPoolRenderer::new
                    );
                }
            }
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(RiverFishPoolModel.LAYER_LOCATION, RiverFishPoolModel::getTexturedModelData);
            event.registerLayerDefinition(OceanFishPoolModel.LAYER_LOCATION, OceanFishPoolModel::getTexturedModelData);
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            for (EggItem eggItem : EggItem.EGG_ITEMS) {
                event.register((stack, tintIndex) -> eggItem.getColor(tintIndex), eggItem);
            }
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(SellingBinTooltipComponent.class, SellingBinClientTooltipComponent::new);
        }
    }

    @Mod.EventBusSubscriber(modid = ListMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onRenderTooltipPre(RenderTooltipEvent.GatherComponents event) {
            SellingBinClientPriceHelper.findRecipe(event.getItemStack()).ifPresent(recipe -> {
                event.getTooltipElements().add(Either.right(
                        new SellingBinTooltipComponent(
                                recipe.input,
                                SellingBinClientPriceHelper.getPreviewOutput(recipe),
                                SellingBinClientPriceHelper.getPriceText(recipe)
                        )
                ));
            });
        }

        @SubscribeEvent
        public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            SellingBinClientPriceCache.clear();
        }
    }
}
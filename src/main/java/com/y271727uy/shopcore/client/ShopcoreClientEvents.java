package com.y271727uy.shopcore.client;

import com.mojang.datafixers.util.Either;
import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.all.ModMenus;
import com.y271727uy.shopcore.client.sellingbin.SellingBinClientPriceCache;
import com.y271727uy.shopcore.client.sellingbin.SellingBinClientPriceHelper;
import com.y271727uy.shopcore.client.screen.SellingBinScreen;
import com.y271727uy.shopcore.integration.jei.tooltip.SellingBinClientTooltipComponent;
import com.y271727uy.shopcore.integration.jei.tooltip.SellingBinTooltipComponent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ShopcoreClientEvents {
    private ShopcoreClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBusEvents {
        private ModBusEvents() {
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(ModMenus.SELLING_BIN.get(), SellingBinScreen::new));
        }

        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(SellingBinTooltipComponent.class, SellingBinClientTooltipComponent::new);
        }
    }

    @Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBusEvents {
        private ForgeBusEvents() {
        }

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


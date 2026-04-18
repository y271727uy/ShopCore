package com.y271727uy.shopcore;

import com.mojang.logging.LogUtils;
import com.y271727uy.shopcore.all.ModBlock;
import com.y271727uy.shopcore.all.ModBlockEntities;
import com.y271727uy.shopcore.all.ModItem;
import com.y271727uy.shopcore.all.ModMenus;
import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.economic.ShopcoreEconomicBootstrap;
import com.y271727uy.shopcore.network.ModMessages;
import com.y271727uy.shopcore.economic.shop_menu.MenuCreate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ShopcoreMod.MODID)
public class ShopcoreMod {
    public static final String MODID = "shopcore";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ShopcoreMod() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlock.BLOCKS.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItem.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModMessages.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModLifecycleEvents {
        private ModLifecycleEvents() {
        }

        @SubscribeEvent
        public static void onConstruct(FMLConstructModEvent event) {
            LOGGER.debug("ShopCore mod construction complete");
        }

        @SubscribeEvent
        public static void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ShopcoreEconomicBootstrap::bootstrap);
        event.enqueueWork(MenuCreate::clear);
        event.enqueueWork(MenuCreate::registerAll);
        LOGGER.info("ShopCore economic system initialized");
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ShopCore server bootstrap complete");
    }
}

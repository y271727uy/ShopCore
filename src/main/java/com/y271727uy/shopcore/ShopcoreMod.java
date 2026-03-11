package com.y271727uy.shopcore;

import com.mojang.logging.LogUtils;
import com.y271727uy.shopcore.economic.ShopcoreEconomicBootstrap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ShopcoreMod.MODID)
public class ShopcoreMod {
    public static final String MODID = "shopcore";
    private static final Logger LOGGER = LogUtils.getLogger();
    public ShopcoreMod() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ShopcoreEconomicBootstrap::bootstrap);
        LOGGER.info("ShopCore economic system initialized");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ShopCore server bootstrap complete");
    }
}

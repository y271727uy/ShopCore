package com.y271727uy.shopcore.all;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeModeTabContents {
    private static final ResourceLocation LIST_TAB_ID = ResourceLocation.fromNamespaceAndPath("list", "list");

    private ModCreativeModeTabContents() {
    }

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!isListTab(event)) {
            return;
        }

        event.accept(ModItem.BANK_CARD.get());
        event.accept(ModItem.PREMIUM_BANK_CARD.get());
        event.accept(ModItem.SELLING_BIN.get());
    }

    private static boolean isListTab(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation tabId = event.getTabKey().location();
        return tabId.equals(LIST_TAB_ID);
    }
}




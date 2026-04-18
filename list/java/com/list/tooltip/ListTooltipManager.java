package com.list.tooltip;

import com.list.ListMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ListMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ListTooltipManager {
    private static final Map<Item, List<Component>> tooltips = new HashMap<>();

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        List<Component> tooltip = tooltips.get(event.getItemStack().getItem());
        if (tooltip != null) {
            event.getToolTip().addAll(tooltip);
        }
    }

    public static void registerTooltip(Item item, List<Component> tooltip) {
        tooltips.put(item, tooltip);
    }
}

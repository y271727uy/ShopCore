package com.y271727uy.shopcore.client;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.api.economic.ShopcorePrices;
import com.y271727uy.shopcore.economic.Price;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PriceTooltipHandler {
	private PriceTooltipHandler() {
	}

	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event) {
		Price price = ShopcorePrices.getPrice(event.getItemStack());
		if (price.isEmpty()) {
			return;
		}

		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.header")
				.withStyle(ChatFormatting.GREEN));
		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.basic", price.basicPrice())
				.withStyle(ChatFormatting.GOLD));
		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.add", price.addPrice())
				.withStyle(ChatFormatting.YELLOW));
		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.reputation", price.reputation())
				.withStyle(ChatFormatting.AQUA));
	}
}


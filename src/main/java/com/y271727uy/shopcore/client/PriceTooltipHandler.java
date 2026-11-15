package com.y271727uy.shopcore.client;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.api.component.economic.ShopcorePrices;
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
		var quote = ShopcorePrices.findPrice(event.getItemStack());
		if (quote.isEmpty()) {
			return;
		}
		var price = quote.get();

		String headerKey = TooltipTitleRegistry.resolveTitleKey(event.getItemStack());

		event.getToolTip().add(Component.translatable(headerKey)
				.withStyle(ChatFormatting.GREEN));
		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.basic", price.baseValue())
				.withStyle(ChatFormatting.GOLD));
		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.add", price.additionalValue())
				.withStyle(ChatFormatting.YELLOW));
		event.getToolTip().add(Component.translatable("tooltip.shopcore.price.reputation", price.reputationValue())
				.withStyle(ChatFormatting.AQUA));
	}
}


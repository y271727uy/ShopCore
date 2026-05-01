package com.y271727uy.shopcore.economic;

import com.y271727uy.shopcore.client.ShopcoreTooltipEntries;
import com.y271727uy.shopcore.client.TooltipTitleRegistry;
import com.y271727uy.shopcore.gameplay.sellingbin.SellingBinSeasonalPriceRules;
import net.minecraft.world.item.Items;

/**
 * Registers built-in economic definitions.
 */
public final class ShopcoreEconomicBootstrap {
	private static boolean bootstrapped;

	private ShopcoreEconomicBootstrap() {
	}

	public static void bootstrap() {
		if (bootstrapped) {
			return;
		}
		bootstrapped = true;

		TooltipTitleRegistry.clear();
		ShopcoreTooltipEntries.registerAll();
		SellingBinSeasonalPriceRules.registerDefaults();
		PriceRegistry.registerItem(Items.EMERALD, 100, 20, 5);
		ShopcorePriceEntries.registerAll();
	}
}

package com.y271727uy.shopcore.api.economic;

import com.y271727uy.shopcore.economic.Price;
import com.y271727uy.shopcore.economic.PriceRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * Public static access point used by other modules.
 */
public final class ShopcorePrices {
	public static final PriceProvider PROVIDER = PriceRegistry::getPrice;

	private ShopcorePrices() {
	}

	public static Price getPrice(ItemStack stack) {
		return PROVIDER.getPrice(stack);
	}

	public static boolean hasPrice(ItemStack stack) {
		return PROVIDER.hasPrice(stack);
	}
}


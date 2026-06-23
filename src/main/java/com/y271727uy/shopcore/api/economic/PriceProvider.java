package com.y271727uy.shopcore.api.economic;

import com.y271727uy.shopcore.economic.price.Price;
import net.minecraft.world.item.ItemStack;

public interface PriceProvider {
	Price getPrice(ItemStack stack);

	default boolean hasPrice(ItemStack stack) {
		return !getPrice(stack).isEmpty();
	}
}


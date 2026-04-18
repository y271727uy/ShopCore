package com.list.item;

import javax.annotation.Nonnull;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RecipeBucketItem extends Item {
	public RecipeBucketItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hasCraftingRemainingItem(@Nonnull ItemStack stack) {
		return true;
	}

	@Override
	public @Nonnull ItemStack getCraftingRemainingItem(@Nonnull ItemStack stack) {
		return new ItemStack(Items.BUCKET);
	}
}

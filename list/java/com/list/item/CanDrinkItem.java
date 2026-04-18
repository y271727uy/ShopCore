package com.list.item;

import com.list.all.ModItems;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CanDrinkItem extends DrinkItem {
	public CanDrinkItem(Properties properties) {
		super(properties);
	}

	@Override
	public @Nonnull ItemStack finishUsingItem(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull LivingEntity livingEntity) {
		ItemStack result = super.finishUsingItem(stack, level, livingEntity);
		return result.isEmpty() ? new ItemStack(ModItems.CAN.get()) : result;
	}
}


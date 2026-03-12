package com.y271727uy.shopcore.api.shop_menu;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public interface MenuProvider {
	Optional<String> getBoundMenuId(Object target);

	boolean canAccept(Object target, ItemStack stack);

	List<ItemStack> getCandidateItems(String menuId);
}


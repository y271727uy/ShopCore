package com.y271727uy.shopcore.shop_menu;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * A menu definition backed by an item tag.
 */
public record MenuDefinition(String menuId, TagKey<Item> itemTag) {
	public MenuDefinition {
		Objects.requireNonNull(menuId, "menuId");
		Objects.requireNonNull(itemTag, "itemTag");
	}

	public boolean matches(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		return stack.is(itemTag);
	}
}


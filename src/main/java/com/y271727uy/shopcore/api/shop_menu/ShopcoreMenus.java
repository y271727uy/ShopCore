package com.y271727uy.shopcore.api.shop_menu;

import com.y271727uy.shopcore.shop_menu.MenuBinding;
import com.y271727uy.shopcore.shop_menu.MenuCreate;
import com.y271727uy.shopcore.shop_menu.MenuDefinition;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ShopcoreMenus {
	public static final MenuProvider PROVIDER = new MenuProvider() {
		@Override
		public Optional<String> getBoundMenuId(Object target) {
			return MenuCreate.getBoundMenuId(target);
		}

		@Override
		public boolean canAccept(Object target, ItemStack stack) {
			return MenuCreate.canAccept(target, stack);
		}

		@Override
		public List<ItemStack> getCandidateItems(String menuId) {
			return MenuCreate.getCandidateItems(menuId);
		}
	};

	private ShopcoreMenus() {
	}

	public static MenuDefinition registerMenu(String menuId, String tagId) {
		return MenuCreate.registerMenu(menuId, tagId);
	}

	public static void bindMenu(Object target, String menuId) {
		MenuCreate.bindMenu(target, menuId);
	}

	public static void unbindMenu(Object target) {
		MenuCreate.unbindMenu(target);
	}

	public static Optional<String> getBoundMenuId(Object target) {
		return PROVIDER.getBoundMenuId(target);
	}

	public static Optional<MenuBinding> getBinding(Object target) {
		Objects.requireNonNull(target, "target");
		return getBoundMenuId(target).map(MenuBinding::new);
	}

	public static boolean canAccept(Object target, ItemStack stack) {
		return PROVIDER.canAccept(target, stack);
	}

	public static boolean canAccept(String menuId, ItemStack stack) {
		return MenuCreate.canAccept(menuId, stack);
	}

	public static List<ItemStack> getCandidateItems(String menuId) {
		return PROVIDER.getCandidateItems(menuId);
	}

	public static Optional<MenuDefinition> getMenu(String menuId) {
		return MenuCreate.getMenu(menuId);
	}
}


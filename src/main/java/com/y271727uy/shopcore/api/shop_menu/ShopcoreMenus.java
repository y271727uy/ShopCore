package com.y271727uy.shopcore.api.shop_menu;

import com.y271727uy.shopcore.core.shop.shopmenu.MenuBinding;
import com.y271727uy.shopcore.core.shop.shopmenu.TooltipMenuCreate;
import com.y271727uy.shopcore.core.shop.shopmenu.MenuDefinition;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ShopcoreMenus {
	public static final MenuProvider PROVIDER = new MenuProvider() {
		@Override
		public Optional<String> getBoundMenuId(Object target) {
			return TooltipMenuCreate.getBoundMenuId(target);
		}

		@Override
		public boolean canAccept(Object target, ItemStack stack) {
			return TooltipMenuCreate.canAccept(target, stack);
		}

		@Override
		public List<ItemStack> getCandidateItems(String menuId) {
			return TooltipMenuCreate.getCandidateItems(menuId);
		}
	};

	private ShopcoreMenus() {
	}

	public static MenuDefinition registerMenu(String menuId, String tagId) {
		return TooltipMenuCreate.registerMenu(menuId, tagId);
	}

	public static void bindMenu(Object target, String menuId) {
		TooltipMenuCreate.bindMenu(target, menuId);
	}

	public static void unbindMenu(Object target) {
		TooltipMenuCreate.unbindMenu(target);
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
		return TooltipMenuCreate.canAccept(menuId, stack);
	}

	public static List<ItemStack> getCandidateItems(String menuId) {
		return PROVIDER.getCandidateItems(menuId);
	}

	public static Optional<MenuDefinition> getMenu(String menuId) {
		return TooltipMenuCreate.getMenu(menuId);
	}
}


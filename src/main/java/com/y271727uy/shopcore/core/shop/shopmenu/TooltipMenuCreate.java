package com.y271727uy.shopcore.core.shop.shopmenu;

import com.y271727uy.shopcore.core.util.ItemReferenceResolver;
import com.y271727uy.shopcore.economic.pricesetting.PriceSettingMenus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Central independent menu module.
 *
 * Menus are defined by an id and one item tag.
 * Future block entities can bind themselves to a menu id through this class or the ShopcoreMenus API.
 */
public final class TooltipMenuCreate {
	private static final Map<String, MenuDefinition> MENUS = new LinkedHashMap<>();
	private static final Map<Object, String> BOUND_MENUS = new IdentityHashMap<>();

	private TooltipMenuCreate() {
	}

	public static void clear() {
		MENUS.clear();
		BOUND_MENUS.clear();
	}

	public static void registerAll() {
		// PriceSetting DSL is the authoritative source for maintained menu membership.
	}

	public static MenuDefinition registerMenu(String menuId, String tagId) {
		Objects.requireNonNull(menuId, "menuId");
		Objects.requireNonNull(tagId, "tagId");

		String normalizedMenuId = normalizeMenuId(menuId);
		MenuDefinition definition = new MenuDefinition(normalizedMenuId, ItemReferenceResolver.resolveItemTag(tagId));
		MENUS.put(normalizedMenuId, definition);
		return definition;
	}

	public static Optional<MenuDefinition> getMenu(String menuId) {
		Objects.requireNonNull(menuId, "menuId");
		return Optional.ofNullable(MENUS.get(normalizeMenuId(menuId)));
	}

	public static List<MenuDefinition> menus() {
		return List.copyOf(MENUS.values());
	}

	public static void bindMenu(Object target, String menuId) {
		Objects.requireNonNull(target, "target");
		String normalizedMenuId = normalizeMenuId(menuId);
		if (!MENUS.containsKey(normalizedMenuId)) {
			throw new IllegalArgumentException("Unknown menu id: " + menuId);
		}
		BOUND_MENUS.put(target, normalizedMenuId);
	}

	public static void unbindMenu(Object target) {
		Objects.requireNonNull(target, "target");
		BOUND_MENUS.remove(target);
	}

	public static Optional<String> getBoundMenuId(Object target) {
		Objects.requireNonNull(target, "target");
		return Optional.ofNullable(BOUND_MENUS.get(target));
	}

	public static boolean hasBoundMenu(Object target) {
		return getBoundMenuId(target).isPresent();
	}

	public static boolean canAccept(Object target, ItemStack stack) {
		Objects.requireNonNull(target, "target");
		Objects.requireNonNull(stack, "stack");
		return getBoundMenuId(target)
				.map(menuId -> canAccept(menuId, stack))
				.orElse(false);
	}

	public static boolean canAccept(String menuId, ItemStack stack) {
		Objects.requireNonNull(menuId, "menuId");
		Objects.requireNonNull(stack, "stack");
		Optional<List<ItemStack>> configuredMenuItems = PriceSettingMenus.getCandidateItemsIfPresent(menuId);
		if (configuredMenuItems.isPresent()) {
			return configuredMenuItems.get().stream()
					.anyMatch(candidate -> ItemStack.isSameItemSameTags(candidate, stack));
		}
		return getMenu(menuId)
				.map(menu -> menu.matches(stack))
				.orElse(false);
	}

	public static List<ItemStack> getCandidateItems(String menuId) {
		Optional<List<ItemStack>> configuredMenuItems = PriceSettingMenus.getCandidateItemsIfPresent(menuId);
		if (configuredMenuItems.isPresent()) {
			return configuredMenuItems.get();
		}
		MenuDefinition definition = getMenu(menuId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown menu id: " + menuId));

		List<ItemStack> candidates = new ArrayList<>();
		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			if (item == null) {
				continue;
			}

			ItemStack stack = new ItemStack(item);
			if (!stack.isEmpty() && definition.matches(stack)) {
				candidates.add(stack);
			}
		}
		return List.copyOf(candidates);
	}

	private static String normalizeMenuId(String menuId) {
		String normalized = Objects.requireNonNull(menuId, "menuId").trim();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("menuId cannot be blank");
		}
		return normalized;
	}
}

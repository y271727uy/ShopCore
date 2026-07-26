package com.y271727uy.shopcore.economic.price;

import com.y271727uy.shopcore.core.util.ItemReferenceResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Central registry for all item price definitions.
 */
public final class PriceRegistry {
	private static final List<PriceDefinition> DEFINITIONS = new ArrayList<>();
	private static final Comparator<PriceDefinition> PRIORITY_ORDER = Comparator
			.comparingInt(PriceDefinition::priority)
			.reversed();

	private PriceRegistry() {
	}

	public static void clear() {
		DEFINITIONS.clear();
	}

	public static PriceDefinition register(PriceDefinition definition) {
		Objects.requireNonNull(definition, "definition");
		DEFINITIONS.add(definition);
		DEFINITIONS.sort(PRIORITY_ORDER);
		return definition;
	}

	public static PriceDefinition registerItem(Item item, int basicPrice, int addPrice, int reputation) {
		return register(PriceDefinition.forItem(item, Price.of(basicPrice, addPrice, reputation)));
	}

	public static PriceDefinition registerItem(String itemId, int basicPrice, int addPrice, int reputation) {
		return registerItem(resolveItem(itemId), basicPrice, addPrice, reputation);
	}

	public static PriceDefinition registerTag(TagKey<Item> tag, int basicPrice, int addPrice, int reputation) {
		return register(PriceDefinition.forTag(tag, Price.of(basicPrice, addPrice, reputation)));
	}

	public static PriceDefinition registerTag(String tagId, int basicPrice, int addPrice, int reputation) {
		return registerTag(resolveItemTag(tagId), basicPrice, addPrice, reputation);
	}

	public static PriceDefinition registerEntry(String entry, int basicPrice, int addPrice, int reputation) {
		Objects.requireNonNull(entry, "entry");
		if (entry.startsWith("#")) {
			return registerTag(entry, basicPrice, addPrice, reputation);
		}
		return registerItem(entry, basicPrice, addPrice, reputation);
	}

	public static PriceDefinition registerCustom(Predicate<ItemStack> matcher, Price price, String description, int priority) {
		return register(PriceDefinition.custom(matcher, price, description, priority));
	}

	public static Optional<Price> findPrice(ItemStack stack) {
		return DEFINITIONS.stream()
				.filter(definition -> definition.matches(stack))
				.map(PriceDefinition::price)
				.findFirst();
	}

	public static Price getPrice(ItemStack stack) {
		return findPrice(stack).orElse(Price.EMPTY);
	}

	public static boolean hasPrice(ItemStack stack) {
		return findPrice(stack).isPresent();
	}

	public static List<PriceDefinition> definitions() {
		return List.copyOf(DEFINITIONS);
	}

	public static Item resolveItem(String itemId) {
		return ItemReferenceResolver.resolveItem(itemId);
	}

	public static TagKey<Item> resolveItemTag(String tagId) {
		return ItemReferenceResolver.resolveItemTag(tagId);
	}

	private static ResourceLocation parseResourceLocation(String rawId, boolean allowHashPrefix) {
		return ItemReferenceResolver.parseResourceLocation(rawId, allowHashPrefix);
	}
}





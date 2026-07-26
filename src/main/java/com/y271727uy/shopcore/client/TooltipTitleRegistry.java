package com.y271727uy.shopcore.client;

import com.y271727uy.shopcore.core.util.ItemReferenceResolver;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Central registry for special tooltip titles matched by item tag.
 * Independent from price registration logic.
 */
public final class TooltipTitleRegistry {
	private static final String DEFAULT_TITLE_KEY = "tooltip.shopcore.price.header";
	private static final List<Entry> ENTRIES = new ArrayList<>();

	private TooltipTitleRegistry() {
	}

	public static void clear() {
		ENTRIES.clear();
	}

	public static void registerTagTitle(String tagId, String translationKey) {
		Objects.requireNonNull(tagId, "tagId");
		Objects.requireNonNull(translationKey, "translationKey");
		TagKey<Item> tag = ItemReferenceResolver.resolveItemTag(tagId);
		ENTRIES.add(new Entry(stack -> stack.is(tag), translationKey));
	}

	public static void registerItemTitle(String itemId, String translationKey) {
		Objects.requireNonNull(itemId, "itemId");
		Objects.requireNonNull(translationKey, "translationKey");
		Item item = ItemReferenceResolver.resolveItem(itemId);
		ENTRIES.add(new Entry(stack -> stack.is(item), translationKey));
	}

	public static String resolveTitleKey(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		Optional<String> matchedTitle = ENTRIES.stream()
				.filter(entry -> entry.matches(stack))
				.map(Entry::translationKey)
				.findFirst();
		return matchedTitle.orElse(DEFAULT_TITLE_KEY);
	}

	private record Entry(Predicate<ItemStack> matcher, String translationKey) {
		private Entry {
			Objects.requireNonNull(matcher, "matcher");
			Objects.requireNonNull(translationKey, "translationKey");
		}

		private boolean matches(ItemStack stack) {
			return matcher.test(stack);
		}
	}
}


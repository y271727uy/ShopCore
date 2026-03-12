package com.y271727uy.shopcore.client;

import com.y271727uy.shopcore.economic.PriceRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
		ENTRIES.add(new Entry(PriceRegistry.resolveItemTag(tagId), translationKey));
	}

	public static String resolveTitleKey(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		Optional<String> matchedTitle = ENTRIES.stream()
				.filter(entry -> entry.matches(stack))
				.map(Entry::translationKey)
				.findFirst();
		return matchedTitle.orElse(DEFAULT_TITLE_KEY);
	}

	private record Entry(TagKey<Item> tag, String translationKey) {
		private Entry {
			Objects.requireNonNull(tag, "tag");
			Objects.requireNonNull(translationKey, "translationKey");
		}

		private boolean matches(ItemStack stack) {
			return stack.is(tag);
		}
	}
}


package com.y271727uy.shopcore.core.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ItemReferenceResolver {
    private ItemReferenceResolver() {
    }

    public static boolean isTagReference(String itemIdOrTagId) {
        return Objects.requireNonNull(itemIdOrTagId, "itemIdOrTagId").trim().startsWith("#");
    }

    public static Item resolveItem(String itemId) {
        ResourceLocation id = parseResourceLocation(itemId, false);
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            throw new IllegalArgumentException("Unknown item id: " + itemId);
        }
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(id), "Resolved item cannot be null: " + itemId);
    }

    public static TagKey<Item> resolveItemTag(String tagId) {
        ResourceLocation id = parseResourceLocation(tagId, true);
        return TagKey.create(Registries.ITEM, id);
    }

    public static List<ItemStack> resolveCandidates(String itemIdOrTagId) {
        Objects.requireNonNull(itemIdOrTagId, "itemIdOrTagId");
        if (isTagReference(itemIdOrTagId)) {
            return resolveCandidates(resolveItemTag(itemIdOrTagId));
        }

        ResourceLocation location = ResourceLocation.tryParse(itemIdOrTagId.trim());
        if (location == null || !ForgeRegistries.ITEMS.containsKey(location)) {
            return List.of();
        }
        Item item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null || item == Items.AIR) {
            return List.of();
        }
        return List.of(new ItemStack(item));
    }

    public static List<ItemStack> resolveCandidates(TagKey<Item> tagKey) {
        Objects.requireNonNull(tagKey, "tagKey");
        List<ItemStack> candidates = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item == null || item == Items.AIR) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty() && stack.is(tagKey)) {
                candidates.add(stack);
            }
        }
        return List.copyOf(candidates);
    }

    public static ResourceLocation parseResourceLocation(String rawId, boolean allowHashPrefix) {
        Objects.requireNonNull(rawId, "rawId");
        String normalized = rawId.trim();
        if (allowHashPrefix && normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        ResourceLocation id = ResourceLocation.tryParse(normalized);
        if (id == null) {
            throw new IllegalArgumentException("Invalid resource location: " + rawId);
        }
        return id;
    }
}

package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.shop.shopmenu.TooltipMenuCreate;
import com.y271727uy.shopcore.economic.price.PriceRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MenuItemCandidates {
    private MenuItemCandidates() {
    }

    public static List<ItemStack> resolve(String menuIdOrTagId) {
        Objects.requireNonNull(menuIdOrTagId, "menuIdOrTagId");
        try {
            return TooltipMenuCreate.getCandidateItems(menuIdOrTagId);
        } catch (IllegalArgumentException ignored) {
        }
        List<ItemStack> itemCandidate = resolveItem(menuIdOrTagId);
        if (!itemCandidate.isEmpty()) {
            return itemCandidate;
        }
        return resolveTag(menuIdOrTagId);
    }

    public static List<ItemStack> resolveItem(String itemId) {
        ResourceLocation location = ResourceLocation.tryParse(Objects.requireNonNull(itemId, "itemId"));
        if (location == null || !ForgeRegistries.ITEMS.containsKey(location)) {
            return List.of();
        }
        Item item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null || item == Items.AIR) {
            return List.of();
        }
        return List.of(new ItemStack(item));
    }

    public static List<ItemStack> resolveTag(String tagId) {
        return resolveTag(PriceRegistry.resolveItemTag(tagId));
    }

    public static List<ItemStack> resolveTag(TagKey<Item> tagKey) {
        Objects.requireNonNull(tagKey, "tagKey");
        List<ItemStack> candidates = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty() && stack.is(tagKey)) {
                candidates.add(stack);
            }
        }
        return List.copyOf(candidates);
    }
}

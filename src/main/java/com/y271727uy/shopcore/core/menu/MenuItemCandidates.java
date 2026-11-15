package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.util.ItemReferenceResolver;
import com.y271727uy.shopcore.core.economic.pricesetting.PriceSettingMenus;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MenuItemCandidates {
    private MenuItemCandidates() {
    }

    public static List<ItemStack> resolve(String menuIdOrTagId) {
        Objects.requireNonNull(menuIdOrTagId, "menuIdOrTagId");
        Optional<List<ItemStack>> configuredMenuItems = PriceSettingMenus.getCandidateItemsIfPresent(menuIdOrTagId);
        if (configuredMenuItems.isPresent()) {
            return configuredMenuItems.get();
        }
        var registeredTag = MenuCatalog.findTag(menuIdOrTagId);
        if (registeredTag.isPresent()) {
            return resolveTag(registeredTag.get().toString());
        }
        List<ItemStack> itemCandidate = resolveItem(menuIdOrTagId);
        if (!itemCandidate.isEmpty()) {
            return itemCandidate;
        }
        return resolveTag(menuIdOrTagId);
    }

    public static List<ItemStack> resolveItem(String itemId) {
        return ItemReferenceResolver.resolveCandidates(itemId);
    }

    public static List<ItemStack> resolveTag(String tagId) {
        return resolveTag(ItemReferenceResolver.resolveItemTag(tagId));
    }

    public static List<ItemStack> resolveTag(TagKey<Item> tagKey) {
        return ItemReferenceResolver.resolveCandidates(tagKey);
    }
}

package com.y271727uy.shopcore.api.component.menu;

import com.y271727uy.shopcore.core.menu.MenuCatalog;
import com.y271727uy.shopcore.core.menu.MenuItemCandidates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Named tag menus and candidate queries, using the same resolver as shop order generation. */
public final class ShopcoreMenus {
    private ShopcoreMenus() { }

    /** Registers or replaces a tag alias. PriceSetting menus take precedence when ids coincide. */
    public static void registerTag(String menuId, String tagId) {
        MenuCatalog.registerTag(menuId, tagId);
    }

    /** Returns the registered tag id without touching Minecraft item registries. */
    public static Optional<ResourceLocation> findTag(String menuId) {
        return MenuCatalog.findTag(menuId);
    }

    /** Resolves a configured menu, tag alias, item id or item tag. No target-object binding is required. */
    public static List<ItemStack> getCandidateItems(String reference) {
        return MenuItemCandidates.resolve(reference).stream().map(ItemStack::copy).toList();
    }

    public static boolean canAccept(String reference, ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return getCandidateItems(reference).stream().anyMatch(item -> ItemStack.isSameItemSameTags(item, stack));
    }
}

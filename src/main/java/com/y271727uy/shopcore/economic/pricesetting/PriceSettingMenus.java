package com.y271727uy.shopcore.economic.pricesetting;

import com.y271727uy.shopcore.core.util.ItemReferenceResolver;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class PriceSettingMenus {
    private static final Map<String, List<String>> MENU_ENTRIES = new LinkedHashMap<>();

    private PriceSettingMenus() {
    }

    public static void clear() {
        MENU_ENTRIES.clear();
    }

    public static void add(String menuId, String itemIdOrTagId) {
        String normalizedMenuId = PriceSettingDefinition.normalizeMenuId(menuId);
        Objects.requireNonNull(itemIdOrTagId, "itemIdOrTagId");
        MENU_ENTRIES.computeIfAbsent(normalizedMenuId, ignored -> new ArrayList<>()).add(itemIdOrTagId.trim());
    }

    public static boolean hasMenu(String menuId) {
        return MENU_ENTRIES.containsKey(PriceSettingDefinition.normalizeMenuId(menuId));
    }

    public static List<String> entries(String menuId) {
        return List.copyOf(MENU_ENTRIES.getOrDefault(PriceSettingDefinition.normalizeMenuId(menuId), List.of()));
    }

    public static Map<String, List<String>> snapshot() {
        Map<String, List<String>> snapshot = new LinkedHashMap<>();
        MENU_ENTRIES.forEach((menuId, entries) -> snapshot.put(menuId, List.copyOf(entries)));
        return Map.copyOf(snapshot);
    }

    public static Optional<List<ItemStack>> getCandidateItemsIfPresent(String menuId) {
        String normalizedMenuId = PriceSettingDefinition.normalizeMenuId(menuId);
        if (!MENU_ENTRIES.containsKey(normalizedMenuId)) {
            return Optional.empty();
        }
        return Optional.of(getCandidateItems(normalizedMenuId));
    }

    public static List<ItemStack> getCandidateItems(String menuId) {
        List<String> entries = entries(menuId);
        if (entries.isEmpty()) {
            return List.of();
        }

        List<ItemStack> candidates = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String entry : entries) {
            for (ItemStack stack : ItemReferenceResolver.resolveCandidates(entry)) {
                String key = String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()));
                if (seen.add(key)) {
                    candidates.add(stack.copyWithCount(1));
                }
            }
        }
        return List.copyOf(candidates);
    }

    public static boolean canAccept(String menuId, ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return getCandidateItemsIfPresent(menuId)
                .map(candidates -> candidates.stream().anyMatch(candidate -> ItemStack.isSameItemSameTags(candidate, stack)))
                .orElse(false);
    }
}

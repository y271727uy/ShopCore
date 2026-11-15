package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.util.ItemReferenceResolver;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Internal registry of tag aliases. Contains definitions only, never world or host objects. */
public final class MenuCatalog {
    private static final Map<String, ResourceLocation> TAGS = new LinkedHashMap<>();

    private MenuCatalog() { }

    public static void clear() { TAGS.clear(); }

    public static void registerTag(String menuId, String tagId) {
        TAGS.put(normalize(menuId), ItemReferenceResolver.parseResourceLocation(tagId, true));
    }

    public static Optional<ResourceLocation> findTag(String menuId) {
        return Optional.ofNullable(TAGS.get(normalize(menuId)));
    }

    private static String normalize(String id) {
        String result = Objects.requireNonNull(id, "menuId").trim();
        if (result.isEmpty()) throw new IllegalArgumentException("menuId cannot be blank");
        return result;
    }
}

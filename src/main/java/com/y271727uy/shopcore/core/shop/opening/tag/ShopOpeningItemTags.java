package com.y271727uy.shopcore.core.shop.opening.tag;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ShopOpeningItemTags {
    public static final TagKey<Item> SAUCE = itemTag("sauce");

    private ShopOpeningItemTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }
}

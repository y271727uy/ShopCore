package com.y271727uy.shopcore.economic.micromachinelearning.weight;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

public final class WeightTags {
    public static final TagKey<Item> WEIGHT_ONE = itemTag("weight_one");
    public static final TagKey<Item> WEIGHT_TWO = itemTag("weight_two");
    public static final TagKey<Item> WEIGHT_THREE = itemTag("weight_three");
    public static final TagKey<Item> WEIGHT_FOUR = itemTag("weight_four");
    public static final TagKey<Item> WEIGHT_FIVE = itemTag("weight_five");
    public static final TagKey<Item> WEIGHT_SIX = itemTag("weight_six");
    public static final TagKey<Item> WEIGHT_SEVEN = itemTag("weight_seven");
    public static final TagKey<Item> WEIGHT_EIGHT = itemTag("weight_eight");
    public static final TagKey<Item> WEIGHT_NINE = itemTag("weight_nine");
    public static final TagKey<Item> WEIGHT_TEN = itemTag("weight_ten");

    public static final List<TagKey<Item>> ORDERED = List.of(
            WEIGHT_ONE,
            WEIGHT_TWO,
            WEIGHT_THREE,
            WEIGHT_FOUR,
            WEIGHT_FIVE,
            WEIGHT_SIX,
            WEIGHT_SEVEN,
            WEIGHT_EIGHT,
            WEIGHT_NINE,
            WEIGHT_TEN
    );

    private WeightTags() {
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path));
    }
}

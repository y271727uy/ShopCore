package com.list.gameplay.fish_group.pool;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public record FishPoolLootEntryDefinition(
        @Nullable ResourceLocation itemId,
        @Nullable ResourceLocation tagId,
        int weight,
        int minCount,
        int maxCount,
        @Nullable String nbt
) {
    public FishPoolLootEntryDefinition {
        if (itemId == null && tagId == null) {
            throw new IllegalArgumentException("Fish pool loot output must define either an item or a tag");
        }
        if (tagId != null) {
            itemId = null;
        }
        weight = Math.max(1, weight);
        minCount = Math.max(1, minCount);
        maxCount = Math.max(minCount, maxCount);
        if (nbt != null && nbt.isBlank()) {
            nbt = null;
        }
    }

    public static FishPoolLootEntryDefinition item(String itemId, int weight, int minCount, int maxCount) {
        return new FishPoolLootEntryDefinition(parse(itemId, "item"), null, weight, minCount, maxCount, null);
    }

    public static FishPoolLootEntryDefinition item(String itemId, int weight) {
        return item(itemId, weight, 1, 1);
    }

    public static FishPoolLootEntryDefinition tag(String tagId, int weight, int minCount, int maxCount) {
        return new FishPoolLootEntryDefinition(null, parse(tagId, "tag"), weight, minCount, maxCount, null);
    }

    public static FishPoolLootEntryDefinition tag(String tagId, int weight) {
        return tag(tagId, weight, 1, 1);
    }

    public FishPoolLootEntryDefinition withNbt(String nbt) {
        return new FishPoolLootEntryDefinition(this.itemId, this.tagId, this.weight, this.minCount, this.maxCount, nbt);
    }

    public boolean usesTag() {
        return this.tagId != null;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (this.tagId != null) {
            json.addProperty("tag", this.tagId.toString());
        } else if (this.itemId != null) {
            json.addProperty("item", this.itemId.toString());
        }

        if (this.weight != 1) {
            json.addProperty("weight", this.weight);
        }

        int min = Mth.clamp(this.minCount, 1, Integer.MAX_VALUE);
        int max = Mth.clamp(this.maxCount, min, Integer.MAX_VALUE);
        if (min != 1 || max != 1) {
            if (min == max) {
                json.addProperty("MaxFishAwarded", max);
            } else {
                JsonObject minRange = new JsonObject();
                minRange.addProperty("min", min);
                minRange.addProperty("max", max);
                json.add("MinRange", minRange);
            }
        }

        if (this.nbt != null) {
            json.addProperty("nbt", this.nbt);
        }
        return json;
    }

    private static ResourceLocation parse(String id, String type) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid fish pool loot " + type + " id: " + id);
        }
        return parsed;
    }
}


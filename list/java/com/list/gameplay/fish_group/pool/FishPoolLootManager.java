package com.list.gameplay.fish_group.pool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.list.ListMod;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class FishPoolLootManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final FishPoolLootManager INSTANCE = new FishPoolLootManager();

    private volatile Map<ResourceLocation, FishPoolLootTable> lootTables = Map.of();

    private FishPoolLootManager() {
        super(GSON, "fish_pool_loot_tables/gameplay/fishing_pools");
    }

    @Override
    protected void apply(@Nonnull Map<ResourceLocation, JsonElement> jsonEntries, @Nonnull ResourceManager resourceManager, @Nonnull ProfilerFiller profiler) {
        java.util.Map<ResourceLocation, FishPoolLootTable> parsedTables = new java.util.HashMap<>();
        jsonEntries.forEach((id, element) -> {
            try {
                parsedTables.put(id, parseTable(id, GsonHelper.convertToJsonObject(element, "fish_pool_loot")));
            } catch (Exception exception) {
                ListMod.LOGGER.error("Failed to parse fish pool loot table {}", id, exception);
            }
        });
        this.lootTables = Map.copyOf(parsedTables);
        ListMod.LOGGER.info("Loaded {} fish pool loot tables", this.lootTables.size());
    }

    public ItemStack rollReward(ResourceLocation poolId, ServerLevel level, RandomSource random) {
        FishPoolLootTable lootTable = this.lootTables.get(poolId);
        if (lootTable == null) {
            return FishPoolFactory.get(poolId)
                    .map(this::fromDefinition)
                    .map(table -> table.roll(level, random))
                    .orElseGet(() -> {
                        ListMod.LOGGER.warn("Missing fish pool loot table for {}", poolId);
                        return ItemStack.EMPTY;
                    });
        }
        return lootTable.roll(level, random);
    }

    private FishPoolLootTable fromDefinition(FishPoolDefinition definition) {
        return new FishPoolLootTable(
                definition.minFishCount(),
                definition.maxFishCount(),
                definition.fishKing(),
                definition.biomes(),
                definition.weatherRequirement(),
                definition.timeRequirement(),
                definition.outputs()
                        .stream()
                        .map(output -> new FishPoolLootEntry(output.itemId(), output.tagId(), output.usesTag(), output.weight(), new CountRange(output.minCount(), output.maxCount()), output.nbt()))
                        .toList()
        );
    }

    private FishPoolLootTable parseTable(ResourceLocation id, JsonObject root) {
        List<FishPoolLootEntry> entries = new ArrayList<>();
        Integer minFishCount = parseRootPositiveInt(root, "MinFishCount", "minFishCount");
        Integer maxFishCount = parseRootPositiveInt(root, "MaxFishCount", "maxFishCount");
        ResourceLocation fishKing = parseRootResourceLocation(id, root, "FishKing", "fishKing");
        List<ResourceLocation> biomes = parseRootBiomeList(id, root, "Biome", "biome");
        FishPoolDefinition.WeatherRequirement weatherRequirement = parseWeatherRequirement(id, root);
        FishPoolDefinition.TimeRequirement timeRequirement = parseTimeRequirement(id, root);

        if (root.has("entries")) {
            addEntries(id, GsonHelper.getAsJsonArray(root, "entries"), entries);
        }

        if (root.has("pools")) {
            JsonArray pools = GsonHelper.getAsJsonArray(root, "pools");
            for (JsonElement poolElement : pools) {
                JsonObject poolObject = GsonHelper.convertToJsonObject(poolElement, "pool");
                if (poolObject.has("entries")) {
                    addEntries(id, GsonHelper.getAsJsonArray(poolObject, "entries"), entries);
                }
            }
        }

        if (entries.isEmpty()) {
            throw new JsonParseException("Fish pool loot table " + id + " does not contain any entries");
        }

        return new FishPoolLootTable(minFishCount, maxFishCount, fishKing, biomes, weatherRequirement, timeRequirement, List.copyOf(entries));
    }

    private void addEntries(ResourceLocation tableId, JsonArray jsonEntries, List<FishPoolLootEntry> parsedEntries) {
        for (JsonElement entryElement : jsonEntries) {
            JsonObject entryObject = GsonHelper.convertToJsonObject(entryElement, "entry");
            FishPoolLootEntry entry = parseEntry(tableId, entryObject);
            if (entry != null) {
                parsedEntries.add(entry);
            }
        }
    }

    @Nullable
    private FishPoolLootEntry parseEntry(ResourceLocation tableId, JsonObject entryObject) {
        String configuredTag = getNullableString(entryObject, "tag");
        String configuredItem = getNullableString(entryObject, "item");

        if ((configuredItem == null || configuredItem.isBlank()) && entryObject.has("name")) {
            configuredItem = getNullableString(entryObject, "name");
        }

        boolean useTag = configuredTag != null && !configuredTag.isBlank();
        ResourceLocation itemId = configuredItem == null || configuredItem.isBlank() ? null : parseResourceLocation(tableId, configuredItem, "item");
        ResourceLocation tagId = useTag ? parseResourceLocation(tableId, configuredTag, "tag") : null;
        CountRange countRange = parseCountRange(entryObject);
        String nbt = parseNbt(entryObject);
        int weight = Math.max(1, GsonHelper.getAsInt(entryObject, "weight", 1));

        if (useTag && tagId == null) {
            ListMod.LOGGER.warn("Fish pool loot table {} has an invalid tag entry: {}", tableId, configuredTag);
            return null;
        }
        if (!useTag && itemId == null) {
            ListMod.LOGGER.warn("Fish pool loot table {} has an entry without a valid item: {}", tableId, entryObject);
            return null;
        }

        return new FishPoolLootEntry(itemId, tagId, useTag, weight, countRange, nbt);
    }

    private CountRange parseCountRange(JsonObject entryObject) {
        CountRange namedRange = parseNamedCountRange(entryObject);
        if (namedRange != null) {
            return namedRange;
        }

        if (entryObject.has("count")) {
            return readCount(entryObject.get("count"));
        }

        if (entryObject.has("functions")) {
            JsonArray functions = GsonHelper.getAsJsonArray(entryObject, "functions");
            for (JsonElement functionElement : functions) {
                JsonObject functionObject = GsonHelper.convertToJsonObject(functionElement, "function");
                String function = GsonHelper.getAsString(functionObject, "function", "");
                if ("minecraft:set_count".equals(function) && functionObject.has("count")) {
                    return readCount(functionObject.get("count"));
                }
            }
        }

        return new CountRange(1, 1);
    }

    @Nullable
    private CountRange parseNamedCountRange(JsonObject entryObject) {
        JsonElement minRangeElement = getFirstPresent(entryObject, "MinRange", "minRange");
        if (minRangeElement != null) {
            return readRange(minRangeElement);
        }

        Integer maxFishAwarded = getFirstPresentInt(entryObject, "MaxFishAwarded", "maxFishAwarded");
        if (maxFishAwarded != null) {
            int clamped = Math.max(1, maxFishAwarded);
            return new CountRange(clamped, clamped);
        }

        return null;
    }

    private CountRange readCount(JsonElement countElement) {
        if (countElement.isJsonPrimitive()) {
            int count = Math.max(1, countElement.getAsInt());
            return new CountRange(count, count);
        }

        JsonObject countObject = GsonHelper.convertToJsonObject(countElement, "count");
        int min = Math.max(1, GsonHelper.getAsInt(countObject, "min", 1));
        int max = Math.max(min, GsonHelper.getAsInt(countObject, "max", min));
        return new CountRange(min, max);
    }

    private CountRange readRange(JsonElement rangeElement) {
        if (rangeElement.isJsonArray()) {
            JsonArray rangeArray = GsonHelper.convertToJsonArray(rangeElement, "MinRange");
            if (rangeArray.size() < 2) {
                throw new JsonParseException("MinRange must contain at least [min, max]");
            }
            int min = Math.max(1, rangeArray.get(0).getAsInt());
            int max = Math.max(min, rangeArray.get(1).getAsInt());
            return new CountRange(min, max);
        }

        return readCount(rangeElement);
    }

    @Nullable
    private String parseNbt(JsonObject entryObject) {
        String directNbt = getNullableString(entryObject, "nbt");
        if (directNbt != null && !directNbt.isBlank()) {
            return directNbt;
        }

        if (entryObject.has("functions")) {
            JsonArray functions = GsonHelper.getAsJsonArray(entryObject, "functions");
            for (JsonElement functionElement : functions) {
                JsonObject functionObject = GsonHelper.convertToJsonObject(functionElement, "function");
                String function = GsonHelper.getAsString(functionObject, "function", "");
                if ("minecraft:set_nbt".equals(function)) {
                    return getNullableString(functionObject, "tag");
                }
            }
        }

        return null;
    }

    @Nullable
    private ResourceLocation parseResourceLocation(ResourceLocation tableId, String rawId, String fieldName) {
        ResourceLocation parsedId = ResourceLocation.tryParse(rawId);
        if (parsedId == null) {
            ListMod.LOGGER.warn("Fish pool loot table {} has invalid {} id {}", tableId, fieldName, rawId);
        }
        return parsedId;
    }

    @Nullable
    private String getNullableString(JsonObject object, String memberName) {
        if (!object.has(memberName)) {
            return null;
        }
        String value = GsonHelper.getAsString(object, memberName, "");
        return value.trim();
    }

    @Nullable
    private ResourceLocation parseRootResourceLocation(ResourceLocation tableId, JsonObject object, String... keys) {
        for (String key : keys) {
            String rawValue = getNullableString(object, key);
            if (rawValue != null && !rawValue.isBlank()) {
                return parseResourceLocation(tableId, rawValue, key);
            }
        }
        return null;
    }

    private List<ResourceLocation> parseRootBiomeList(ResourceLocation tableId, JsonObject object, String... keys) {
        for (String key : keys) {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                continue;
            }

            JsonElement value = object.get(key);
            if (value.isJsonArray()) {
                List<ResourceLocation> parsedBiomes = new ArrayList<>();
                JsonArray array = GsonHelper.convertToJsonArray(value, key);
                for (JsonElement entry : array) {
                    if (entry == null || entry.isJsonNull()) {
                        continue;
                    }
                    String rawId = entry.getAsString().trim();
                    if (rawId.isBlank()) {
                        continue;
                    }
                    ResourceLocation parsed = parseResourceLocation(tableId, rawId, key);
                    if (parsed != null && !parsedBiomes.contains(parsed)) {
                        parsedBiomes.add(parsed);
                    }
                }
                return List.copyOf(parsedBiomes);
            }

            String rawValue = GsonHelper.convertToString(value, key).trim();
            if (rawValue.isBlank()) {
                return List.of();
            }
            ResourceLocation parsed = parseResourceLocation(tableId, rawValue, key);
            return parsed == null ? List.of() : List.of(parsed);
        }
        return List.of();
    }

    @Nullable
    private FishPoolDefinition.WeatherRequirement parseWeatherRequirement(ResourceLocation tableId, JsonObject root) {
        String configuredWeather = getNullableString(root, "WeatherRequirement");
        if (configuredWeather == null || configuredWeather.isBlank()) {
            configuredWeather = getNullableString(root, "weatherRequirement");
        }
        if (configuredWeather == null || configuredWeather.isBlank() || "null".equalsIgnoreCase(configuredWeather)) {
            return null;
        }

        try {
            return FishPoolDefinition.WeatherRequirement.valueOf(configuredWeather.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            ListMod.LOGGER.warn("Fish pool loot table {} has invalid WeatherRequirement {}", tableId, configuredWeather);
            return null;
        }
    }

    @Nullable
    private FishPoolDefinition.TimeRequirement parseTimeRequirement(ResourceLocation tableId, JsonObject root) {
        String configuredTime = getNullableString(root, "TimeRequirement");
        if (configuredTime == null || configuredTime.isBlank()) {
            configuredTime = getNullableString(root, "timeRequirement");
        }
        if (configuredTime == null || configuredTime.isBlank() || "null".equalsIgnoreCase(configuredTime)) {
            return null;
        }

        return switch (configuredTime.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "day" -> FishPoolDefinition.TimeRequirement.DAY;
            case "night" -> FishPoolDefinition.TimeRequirement.NIGHT;
            default -> {
                ListMod.LOGGER.warn("Fish pool loot table {} has invalid TimeRequirement {}", tableId, configuredTime);
                yield null;
            }
        };
    }

    @Nullable
    private JsonElement getFirstPresent(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return object.get(key);
            }
        }
        return null;
    }

    @Nullable
    private Integer getFirstPresentInt(JsonObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return GsonHelper.getAsInt(object, key, 1);
            }
        }
        return null;
    }

    @Nullable
    private Integer parseRootPositiveInt(JsonObject object, String... keys) {
        Integer parsed = getFirstPresentInt(object, keys);
        return parsed == null ? null : Math.max(1, parsed);
    }

    public FishPoolDefinition resolveDefinition(FishPoolDefinition definition) {
        FishPoolLootTable lootTable = this.lootTables.get(definition.id());
        if (lootTable == null) {
            return definition;
        }

        Integer tableMinFishCount = lootTable.minFishCount();
        Integer tableMaxFishCount = lootTable.maxFishCount();
        int maxFishCount = tableMaxFishCount != null ? tableMaxFishCount : definition.maxFishCount();
        int minFishCount = tableMinFishCount != null
                ? tableMinFishCount
                : tableMaxFishCount != null
                ? tableMaxFishCount
                : definition.minFishCount();
        minFishCount = Math.max(1, minFishCount);
        maxFishCount = Math.max(minFishCount, maxFishCount);
        ResourceLocation fishKing = lootTable.fishKing() != null ? lootTable.fishKing() : definition.fishKing();
        List<ResourceLocation> biomes = lootTable.biomes().isEmpty() ? definition.biomes() : lootTable.biomes();
        FishPoolDefinition.WeatherRequirement weatherRequirement = lootTable.weatherRequirement() != null
                ? lootTable.weatherRequirement()
                : definition.weatherRequirement();
        FishPoolDefinition.TimeRequirement timeRequirement = lootTable.timeRequirement() != null
                ? lootTable.timeRequirement()
                : definition.timeRequirement();

        if (minFishCount == definition.minFishCount()
                && maxFishCount == definition.maxFishCount()
                && java.util.Objects.equals(fishKing, definition.fishKing())
                && java.util.Objects.equals(biomes, definition.biomes())
                && weatherRequirement == definition.weatherRequirement()
                && timeRequirement == definition.timeRequirement()) {
            return definition;
        }

        return new FishPoolDefinition(
                definition.id(),
                definition.environment(),
                minFishCount,
                maxFishCount,
                fishKing,
                biomes,
                weatherRequirement,
                timeRequirement,
                definition.outputs()
        );
    }

    private record FishPoolLootTable(
            @Nullable Integer minFishCount,
            @Nullable Integer maxFishCount,
            @Nullable ResourceLocation fishKing,
            List<ResourceLocation> biomes,
            @Nullable FishPoolDefinition.WeatherRequirement weatherRequirement,
            @Nullable FishPoolDefinition.TimeRequirement timeRequirement,
            List<FishPoolLootEntry> entries
    ) {
        ItemStack roll(ServerLevel level, RandomSource random) {
            int totalWeight = this.entries.stream().mapToInt(FishPoolLootEntry::weight).sum();
            if (totalWeight <= 0) {
                return ItemStack.EMPTY;
            }

            int roll = random.nextInt(totalWeight);
            for (FishPoolLootEntry entry : this.entries) {
                roll -= entry.weight();
                if (roll < 0) {
                    return entry.createStack(level, random);
                }
            }
            return ItemStack.EMPTY;
        }
    }

    private record FishPoolLootEntry(
            @Nullable ResourceLocation itemId,
            @Nullable ResourceLocation tagId,
            boolean useTag,
            int weight,
            CountRange countRange,
            @Nullable String nbt
    ) {
        ItemStack createStack(ServerLevel level, RandomSource random) {
            Item item = this.resolveItem(level, random);
            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }

            ItemStack stack = new ItemStack(item, Mth.nextInt(random, this.countRange.min(), this.countRange.max()));
            if (this.nbt != null && !this.nbt.isBlank()) {
                try {
                    CompoundTag parsedTag = TagParser.parseTag(this.nbt);
                    stack.setTag(parsedTag);
                } catch (CommandSyntaxException exception) {
                    ListMod.LOGGER.warn("Failed to parse fish pool reward nbt {}", this.nbt, exception);
                }
            }
            return stack;
        }

        @Nullable
        private Item resolveItem(ServerLevel level, RandomSource random) {
            if (this.useTag) {
                if (this.tagId == null) {
                    return null;
                }
                Registry<Item> itemRegistry = level.registryAccess().registryOrThrow(Registries.ITEM);
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, this.tagId);
                List<Item> taggedItems = itemRegistry.getTag(tagKey)
                        .stream()
                        .flatMap(named -> named.stream().map(Holder::value))
                        .filter(item -> item != Items.AIR)
                        .toList();
                if (taggedItems.isEmpty()) {
                    ListMod.LOGGER.warn("Fish pool tag {} does not contain any items", this.tagId);
                    return null;
                }
                return taggedItems.get(random.nextInt(taggedItems.size()));
            }

            if (this.itemId == null) {
                return null;
            }
            return ForgeRegistries.ITEMS.getValue(this.itemId);
        }
    }

    private record CountRange(int min, int max) {
    }
}





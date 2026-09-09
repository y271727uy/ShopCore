package com.y271727uy.shopcore.gameplay.sellingbin;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.gameplay.daily.AbstractShopDailySavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class AgricultureDailySavedData extends AbstractShopDailySavedData {
    private static final String DATA_NAME = ShopcoreMod.MODID + "_agriculture_daily";
    private static final String FILLER_NEWS_TAG = "FillerNews";
    private static final String DECREASES_TAG = "Decreases";
    private static final String INCREASES_TAG = "Increases";
    private static final String ITEM_TAG = "Item";
    private static final String BONUS_TAG = "Bonus";
    private static final int MAX_MARKET_ENTRIES = 3;
    public static final int FILLER_NEWS_COUNT = 30;

    private int fillerNewsIndex = 1;
    private List<MarketEntry> decreases = List.of();
    private List<MarketEntry> increases = List.of();

    static AgricultureDailySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(AgricultureDailySavedData::load, AgricultureDailySavedData::new, DATA_NAME);
    }

    private static AgricultureDailySavedData load(CompoundTag tag) {
        AgricultureDailySavedData data = new AgricultureDailySavedData();
        data.loadEditionDay(tag);
        data.fillerNewsIndex = Math.max(1, Math.min(FILLER_NEWS_COUNT, tag.getInt(FILLER_NEWS_TAG)));
        data.decreases = loadEntries(tag.getList(DECREASES_TAG, Tag.TAG_COMPOUND));
        data.increases = loadEntries(tag.getList(INCREASES_TAG, Tag.TAG_COMPOUND));
        return data;
    }

    private static List<MarketEntry> loadEntries(ListTag tags) {
        return tags.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .filter(tag -> tag.contains(ITEM_TAG, Tag.TAG_STRING) && tag.contains(BONUS_TAG, Tag.TAG_INT))
                .map(tag -> new MarketEntry(ResourceLocation.tryParse(tag.getString(ITEM_TAG)), tag.getInt(BONUS_TAG)))
                .filter(entry -> entry.itemId() != null && entry.bonus() != 0)
                .limit(MAX_MARKET_ENTRIES)
                .toList();
    }

    DailyEdition getOrCreateEdition(long currentDay, Map<ResourceLocation, Integer> currentDecreases, Map<ResourceLocation, Integer> currentIncreases, int fillerNewsIndex) {
        if (needsNewEdition(currentDay)) {
            this.fillerNewsIndex = Math.max(1, Math.min(FILLER_NEWS_COUNT, fillerNewsIndex));
            decreases = selectLargest(currentDecreases, true);
            increases = selectLargest(currentIncreases, false);
            markEditionCreated(currentDay);
        }
        return new DailyEdition(decreases, increases, this.fillerNewsIndex);
    }

    private static List<MarketEntry> selectLargest(Map<ResourceLocation, Integer> bonuses, boolean decreases) {
        return bonuses.entrySet().stream()
                .filter(entry -> decreases ? entry.getValue() < 0 : entry.getValue() > 0)
                .sorted(Comparator.<Map.Entry<ResourceLocation, Integer>>comparingInt(entry -> Math.abs(entry.getValue())).reversed()
                        .thenComparing(entry -> entry.getKey().toString()))
                .limit(MAX_MARKET_ENTRIES)
                .map(entry -> new MarketEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        saveEditionDay(tag);
        tag.putInt(FILLER_NEWS_TAG, fillerNewsIndex);
        tag.put(DECREASES_TAG, saveEntries(decreases));
        tag.put(INCREASES_TAG, saveEntries(increases));
        return tag;
    }

    private static ListTag saveEntries(List<MarketEntry> entries) {
        ListTag tags = new ListTag();
        for (MarketEntry entry : entries) {
            CompoundTag tag = new CompoundTag();
            tag.putString(ITEM_TAG, entry.itemId().toString());
            tag.putInt(BONUS_TAG, entry.bonus());
            tags.add(tag);
        }
        return tags;
    }

    public record MarketEntry(ResourceLocation itemId, int bonus) {
    }

    public record DailyEdition(List<MarketEntry> decreases, List<MarketEntry> increases, int fillerNewsIndex) {
    }
}

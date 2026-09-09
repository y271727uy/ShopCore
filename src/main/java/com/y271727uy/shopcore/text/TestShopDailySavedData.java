package com.y271727uy.shopcore.text;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.gameplay.daily.AbstractShopDailySavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Daily newspaper data sourced exclusively from customer deliveries at the test shop. */
public final class TestShopDailySavedData extends AbstractShopDailySavedData {
    private static final String DATA_NAME = ShopcoreMod.MODID + "_test_shop_daily";
    private static final String SALES_DAY_TAG = "SalesDay";
    private static final String SALES_TAG = "Sales";
    private static final String ITEM_TAG = "Item";
    private static final String COUNT_TAG = "Count";
    private static final String PENDING_SPOILED_FOOD_TAG = "PendingSpoiledFood";
    private static final String NEWS_TAG = "News";
    private static final String NEWS_TYPE_TAG = "Type";
    private static final long UNINITIALIZED_DAY = Long.MIN_VALUE;
    private static final int OVERSUPPLY_THRESHOLD = 8;

    private long salesDay = UNINITIALIZED_DAY;
    private boolean pendingSpoiledFood;
    private final Map<ResourceLocation, Integer> sales = new HashMap<>();
    private List<NewsEntry> edition = List.of();

    public static TestShopDailySavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(TestShopDailySavedData::load, TestShopDailySavedData::new, DATA_NAME);
    }

    public static void recordDelivery(ServerLevel level, ItemStack delivered, int count, boolean spoiled) {
        if (delivered.isEmpty() || count <= 0) {
            return;
        }
        TestShopDailySavedData data = get(level);
        long currentDay = Math.floorDiv(level.getDayTime(), 24000L);
        if (data.salesDay != currentDay) {
            data.salesDay = currentDay;
            data.sales.clear();
        }
        data.sales.merge(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(delivered.getItem()), count, Integer::sum);
        data.pendingSpoiledFood |= spoiled;
        data.setDirty();
    }

    public DailyEdition getOrCreateEdition(long currentDay, RandomSource random) {
        if (needsNewEdition(currentDay)) {
            List<NewsEntry> news = new ArrayList<>();
            if (salesDay == currentDay && !sales.isEmpty()) {
                Map.Entry<ResourceLocation, Integer> leadingSale = sales.entrySet().stream()
                        .max(Comparator.<Map.Entry<ResourceLocation, Integer>>comparingInt(Map.Entry::getValue)
                                .thenComparing(entry -> entry.getKey().toString()))
                        .orElseThrow();
                news.add(leadingSale.getValue() >= OVERSUPPLY_THRESHOLD
                        ? NewsEntry.oversupplied(leadingSale.getKey())
                        : NewsEntry.popular(leadingSale.getKey()));
            } else {
                news.add(NewsEntry.stable());
            }
            if (pendingSpoiledFood) {
                news.add(NewsEntry.spoiledFood());
                pendingSpoiledFood = false;
            }
            news.add(NewsEntry.filler(random.nextInt(3) + 1));
            Collections.shuffle(news, new java.util.Random(random.nextLong()));
            edition = List.copyOf(news);
            markEditionCreated(currentDay);
        }
        return new DailyEdition(edition);
    }

    private static TestShopDailySavedData load(CompoundTag tag) {
        TestShopDailySavedData data = new TestShopDailySavedData();
        data.loadEditionDay(tag);
        data.salesDay = tag.contains(SALES_DAY_TAG, Tag.TAG_LONG) ? tag.getLong(SALES_DAY_TAG) : UNINITIALIZED_DAY;
        data.pendingSpoiledFood = tag.getBoolean(PENDING_SPOILED_FOOD_TAG);
        for (Tag entryTag : tag.getList(SALES_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) entryTag;
            ResourceLocation itemId = ResourceLocation.tryParse(entry.getString(ITEM_TAG));
            if (itemId != null && entry.getInt(COUNT_TAG) > 0) {
                data.sales.put(itemId, entry.getInt(COUNT_TAG));
            }
        }
        data.edition = tag.getList(NEWS_TAG, Tag.TAG_COMPOUND).stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .map(entry -> new NewsEntry(NewsType.valueOf(entry.getString(NEWS_TYPE_TAG)),
                        ResourceLocation.tryParse(entry.getString(ITEM_TAG)), entry.getInt(COUNT_TAG)))
                .toList();
        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        saveEditionDay(tag);
        tag.putLong(SALES_DAY_TAG, salesDay);
        tag.putBoolean(PENDING_SPOILED_FOOD_TAG, pendingSpoiledFood);
        ListTag savedSales = new ListTag();
        sales.forEach((itemId, count) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString(ITEM_TAG, itemId.toString());
            entry.putInt(COUNT_TAG, count);
            savedSales.add(entry);
        });
        tag.put(SALES_TAG, savedSales);
        ListTag savedNews = new ListTag();
        for (NewsEntry entry : edition) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(NEWS_TYPE_TAG, entry.type().name());
            if (entry.itemId() != null) entryTag.putString(ITEM_TAG, entry.itemId().toString());
            entryTag.putInt(COUNT_TAG, entry.count());
            savedNews.add(entryTag);
        }
        tag.put(NEWS_TAG, savedNews);
        return tag;
    }

    public record DailyEdition(List<NewsEntry> entries) {
        public DailyEdition { entries = List.copyOf(entries); }
    }

    public record NewsEntry(NewsType type, ResourceLocation itemId, int count) {
        static NewsEntry popular(ResourceLocation itemId) { return new NewsEntry(NewsType.POPULAR, itemId, 0); }
        static NewsEntry oversupplied(ResourceLocation itemId) { return new NewsEntry(NewsType.OVERSUPPLIED, itemId, 0); }
        static NewsEntry stable() { return new NewsEntry(NewsType.STABLE, null, 0); }
        static NewsEntry spoiledFood() { return new NewsEntry(NewsType.SPOILED_FOOD, null, 0); }
        static NewsEntry filler(int index) { return new NewsEntry(NewsType.FILLER, null, index); }
    }

    public enum NewsType { POPULAR, OVERSUPPLIED, STABLE, SPOILED_FOOD, FILLER }
}

package com.y271727uy.shopcore.gameplay.sellingbin;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class SellingBinMarketSavedData extends SavedData {
    private static final String DATA_NAME = ShopcoreMod.MODID + "_selling_bin_market";
    private static final String FLOATING_BONUSES_TAG = "PriceBonuses";
    private static final String SEASONAL_BONUSES_TAG = "SeasonalPriceBonuses";
    private static final long UNINITIALIZED_DAY = Long.MIN_VALUE;

    private final Map<ResourceLocation, Integer> floatingPriceBonusByRecipe = new HashMap<>();
    private final Map<ResourceLocation, Integer> seasonalPriceBonusByRecipe = new HashMap<>();
    private final Map<ResourceLocation, Integer> carryStageByRecipe = new HashMap<>();
    private long lastProcessedDay = UNINITIALIZED_DAY;

    @SuppressWarnings("resource")
    static SellingBinMarketSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(SellingBinMarketSavedData::load, SellingBinMarketSavedData::new, DATA_NAME);
    }

    static SellingBinMarketSavedData load(CompoundTag tag) {
        SellingBinMarketSavedData data = new SellingBinMarketSavedData();
        if (tag.contains("LastProcessedDay", Tag.TAG_LONG)) {
            data.lastProcessedDay = tag.getLong("LastProcessedDay");
        }

        loadBonusList(tag.getList(FLOATING_BONUSES_TAG, Tag.TAG_COMPOUND), data.floatingPriceBonusByRecipe, data.carryStageByRecipe);
        loadBonusList(tag.getList(SEASONAL_BONUSES_TAG, Tag.TAG_COMPOUND), data.seasonalPriceBonusByRecipe, null);
        return data;
    }

    private static void loadBonusList(ListTag bonuses, Map<ResourceLocation, Integer> targetBonuses, Map<ResourceLocation, Integer> carryStages) {
        for (Tag element : bonuses) {
            CompoundTag bonusTag = (CompoundTag) element;
            if (!bonusTag.contains("Recipe", Tag.TAG_STRING)) {
                continue;
            }

            ResourceLocation recipeId = ResourceLocation.tryParse(bonusTag.getString("Recipe"));
            if (recipeId == null) {
                continue;
            }

            int bonus = bonusTag.getInt("Bonus");
            if (bonus == 0) {
                continue;
            }

            targetBonuses.put(recipeId, bonus);
            if (carryStages != null) {
                int carryStage = bonusTag.contains("CarryStage", Tag.TAG_INT) ? Math.max(0, bonusTag.getInt("CarryStage")) : 0;
                carryStages.put(recipeId, carryStage);
            }
        }
    }

    boolean isInitialized() {
        return lastProcessedDay != UNINITIALIZED_DAY;
    }

    long getLastProcessedDay() {
        return lastProcessedDay;
    }

    void setLastProcessedDay(long dayIndex) {
        if (lastProcessedDay != dayIndex) {
            lastProcessedDay = dayIndex;
            setDirty();
        }
    }

    int getPriceBonus(ResourceLocation recipeId) {
        long totalBonus = (long) getFloatingPriceBonus(recipeId) + getSeasonalPriceBonus(recipeId);
        if (totalBonus <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (totalBonus >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) totalBonus;
    }

    int getFloatingPriceBonus(ResourceLocation recipeId) {
        return floatingPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    boolean setFloatingPriceBonus(ResourceLocation recipeId, int bonus) {
        int currentBonus = getFloatingPriceBonus(recipeId);
        if (currentBonus == bonus) {
            return false;
        }

        if (bonus == 0) {
            floatingPriceBonusByRecipe.remove(recipeId);
            carryStageByRecipe.remove(recipeId);
        } else {
            floatingPriceBonusByRecipe.put(recipeId, bonus);
        }

        setDirty();
        return true;
    }

    int getSeasonalPriceBonus(ResourceLocation recipeId) {
        return seasonalPriceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    boolean setSeasonalPriceBonus(ResourceLocation recipeId, int bonus) {
        int currentBonus = getSeasonalPriceBonus(recipeId);
        if (currentBonus == bonus) {
            return false;
        }

        if (bonus == 0) {
            seasonalPriceBonusByRecipe.remove(recipeId);
        } else {
            seasonalPriceBonusByRecipe.put(recipeId, bonus);
        }

        setDirty();
        return true;
    }

    boolean setSeasonalPriceBonuses(Map<ResourceLocation, Integer> bonuses) {
        Map<ResourceLocation, Integer> normalizedBonuses = new HashMap<>();
        bonuses.forEach((recipeId, bonus) -> {
            if (bonus != 0) {
                normalizedBonuses.put(recipeId, bonus);
            }
        });

        if (seasonalPriceBonusByRecipe.equals(normalizedBonuses)) {
            return false;
        }

        seasonalPriceBonusByRecipe.clear();
        seasonalPriceBonusByRecipe.putAll(normalizedBonuses);
        setDirty();
        return true;
    }

    int getCarryStage(ResourceLocation recipeId) {
        return Math.max(0, carryStageByRecipe.getOrDefault(recipeId, 0));
    }

    boolean setCarryStage(ResourceLocation recipeId, int carryStage) {
        int normalizedStage = Math.max(0, carryStage);
        if (!floatingPriceBonusByRecipe.containsKey(recipeId)) {
            normalizedStage = 0;
        }

        int currentStage = getCarryStage(recipeId);
        if (currentStage == normalizedStage) {
            return false;
        }

        if (normalizedStage == 0) {
            carryStageByRecipe.remove(recipeId);
        } else {
            carryStageByRecipe.put(recipeId, normalizedStage);
        }

        setDirty();
        return true;
    }

    Map<ResourceLocation, Integer> snapshotCarryStages() {
        return new HashMap<>(carryStageByRecipe);
    }

    boolean clearFloatingAdjustments() {
        if (floatingPriceBonusByRecipe.isEmpty() && carryStageByRecipe.isEmpty()) {
            return false;
        }

        floatingPriceBonusByRecipe.clear();
        carryStageByRecipe.clear();
        setDirty();
        return true;
    }

    boolean clearSeasonalAdjustments() {
        if (seasonalPriceBonusByRecipe.isEmpty()) {
            return false;
        }

        seasonalPriceBonusByRecipe.clear();
        setDirty();
        return true;
    }

    Map<ResourceLocation, Integer> snapshotFloatingPriceBonuses() {
        return new HashMap<>(floatingPriceBonusByRecipe);
    }

    Map<ResourceLocation, Integer> snapshotSeasonalPriceBonuses() {
        return new HashMap<>(seasonalPriceBonusByRecipe);
    }

    Map<ResourceLocation, Integer> snapshotPriceBonuses() {
        Map<ResourceLocation, Integer> totalBonuses = snapshotSeasonalPriceBonuses();
        floatingPriceBonusByRecipe.forEach((recipeId, bonus) -> totalBonuses.merge(recipeId, bonus, (left, right) -> {
            long sum = (long) left + right;
            if (sum <= Integer.MIN_VALUE) {
                return Integer.MIN_VALUE;
            }
            if (sum >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) sum;
        }));
        totalBonuses.entrySet().removeIf(entry -> entry.getValue() == 0);
        return totalBonuses;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.putLong("LastProcessedDay", lastProcessedDay);

        ListTag floatingBonuses = new ListTag();
        floatingPriceBonusByRecipe.forEach((recipeId, bonus) -> {
            if (bonus == 0) {
                return;
            }

            CompoundTag bonusTag = new CompoundTag();
            bonusTag.putString("Recipe", recipeId.toString());
            bonusTag.putInt("Bonus", bonus);
            bonusTag.putInt("CarryStage", getCarryStage(recipeId));
            floatingBonuses.add(bonusTag);
        });
        tag.put(FLOATING_BONUSES_TAG, floatingBonuses);

        ListTag seasonalBonuses = new ListTag();
        seasonalPriceBonusByRecipe.forEach((recipeId, bonus) -> {
            if (bonus == 0) {
                return;
            }

            CompoundTag bonusTag = new CompoundTag();
            bonusTag.putString("Recipe", recipeId.toString());
            bonusTag.putInt("Bonus", bonus);
            seasonalBonuses.add(bonusTag);
        });
        tag.put(SEASONAL_BONUSES_TAG, seasonalBonuses);
        return tag;
    }
}

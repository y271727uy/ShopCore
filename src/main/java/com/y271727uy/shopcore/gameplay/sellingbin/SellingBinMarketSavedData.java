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
    private static final long UNINITIALIZED_DAY = Long.MIN_VALUE;

    private final Map<ResourceLocation, Integer> priceBonusByRecipe = new HashMap<>();
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

        ListTag bonuses = tag.getList("PriceBonuses", Tag.TAG_COMPOUND);
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
            if (bonus != 0) {
                data.priceBonusByRecipe.put(recipeId, bonus);
                int carryStage = bonusTag.contains("CarryStage", Tag.TAG_INT) ? Math.max(0, bonusTag.getInt("CarryStage")) : 0;
                data.carryStageByRecipe.put(recipeId, carryStage);
            }
        }
        return data;
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
        return priceBonusByRecipe.getOrDefault(recipeId, 0);
    }

    boolean setPriceBonus(ResourceLocation recipeId, int bonus) {
        int currentBonus = getPriceBonus(recipeId);
        if (currentBonus == bonus) {
            return false;
        }

        if (bonus == 0) {
            priceBonusByRecipe.remove(recipeId);
            carryStageByRecipe.remove(recipeId);
        } else {
            priceBonusByRecipe.put(recipeId, bonus);
        }

        setDirty();
        return true;
    }

    int getCarryStage(ResourceLocation recipeId) {
        return Math.max(0, carryStageByRecipe.getOrDefault(recipeId, 0));
    }

    boolean setCarryStage(ResourceLocation recipeId, int carryStage) {
        int normalizedStage = Math.max(0, carryStage);
        if (!priceBonusByRecipe.containsKey(recipeId)) {
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

    boolean clearActiveAdjustments() {
        if (priceBonusByRecipe.isEmpty() && carryStageByRecipe.isEmpty()) {
            return false;
        }

        priceBonusByRecipe.clear();
        carryStageByRecipe.clear();
        setDirty();
        return true;
    }

    Map<ResourceLocation, Integer> snapshotPriceBonuses() {
        return new HashMap<>(priceBonusByRecipe);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        tag.putLong("LastProcessedDay", lastProcessedDay);

        ListTag bonuses = new ListTag();
        priceBonusByRecipe.forEach((recipeId, bonus) -> {
            if (bonus == 0) {
                return;
            }

            CompoundTag bonusTag = new CompoundTag();
            bonusTag.putString("Recipe", recipeId.toString());
            bonusTag.putInt("Bonus", bonus);
            bonusTag.putInt("CarryStage", getCarryStage(recipeId));
            bonuses.add(bonusTag);
        });
        tag.put("PriceBonuses", bonuses);
        return tag;
    }
}

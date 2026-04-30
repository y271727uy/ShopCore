package com.y271727uy.shopcore.gameplay.sellingbin;

import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.recipe.SellingBinRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.*;

public final class SellingBinGroupManager {
    public static final long DAY_LENGTH_TICKS = 24000L;
    private static final int DAILY_INCREASE_PICK_MIN = 1;
    private static final int DAILY_INCREASE_PICK_MAX = 3;
    private static final int DAILY_INCREASE_AMOUNT_MIN = 1;
    private static final int DAILY_INCREASE_AMOUNT_MAX = 4;
    private static final int DAILY_DECREASE_PICK_MIN = 1;
    private static final int DAILY_DECREASE_PICK_MAX = 3;
    private static final int DAILY_DECREASE_AMOUNT_MIN = 1;
    private static final int DAILY_DECREASE_AMOUNT_MAX = 3;
    private static final float FIRST_CARRY_CHANCE = 0.25F;
    private static final float SECOND_CARRY_CHANCE = 0.10F;
    private static final Object GROUP_CACHE_LOCK = new Object();

    private static Map<String, SellingBinGroup> cachedGroups;

    private SellingBinGroupManager() {
    }

    public static int getPriceBonus(ServerLevel level, ResourceLocation recipeId) {
        return SellingBinMarketSavedData.get(level).getPriceBonus(recipeId);
    }

    public static Map<ResourceLocation, Integer> snapshotPriceBonuses(ServerLevel level) {
        return SellingBinMarketSavedData.get(level).snapshotPriceBonuses();
    }

    public static void invalidateCachedGroups() {
        synchronized (GROUP_CACHE_LOCK) {
            cachedGroups = null;
        }
    }

    public static boolean refreshForElapsedDays(ServerLevel level) {
        SellingBinMarketSavedData marketData = SellingBinMarketSavedData.get(level);
        long currentDay = Math.floorDiv(level.getDayTime(), DAY_LENGTH_TICKS);
        Map<String, SellingBinGroup> groups = collectGroups(level);

        if (!marketData.isInitialized()) {
            // Treat a brand-new market as if the previous processed day was yesterday,
            // so the first server tick actually generates the initial price snapshot.
            marketData.setLastProcessedDay(currentDay - 1L);
        }

        long lastProcessedDay = marketData.getLastProcessedDay();
        if (currentDay <= lastProcessedDay) {
            return false;
        }

        boolean updatedPrices = false;
        for (long day = lastProcessedDay + 1; day <= currentDay; day++) {
            Map<ResourceLocation, Integer> previousBonuses = marketData.snapshotPriceBonuses();
            Map<ResourceLocation, Integer> previousCarryStages = marketData.snapshotCarryStages();
            updatedPrices |= marketData.clearActiveAdjustments();
            for (SellingBinGroup group : groups.values()) {
                updatedPrices |= applyDailyGroupAdjustments(level, marketData, group, previousBonuses, previousCarryStages);
            }
        }

        marketData.setLastProcessedDay(currentDay);
        return updatedPrices;
    }

    public static Map<String, SellingBinGroup> collectGroups(ServerLevel level) {
        synchronized (GROUP_CACHE_LOCK) {
            if (cachedGroups == null) {
                cachedGroups = buildGroups(level);
            }
            return Collections.unmodifiableMap(cachedGroups);
        }
    }

    private static Map<String, SellingBinGroup> buildGroups(ServerLevel level) {
        Map<String, SellingBinGroup> groups = new LinkedHashMap<>();

        for (SellingBinRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.SELLING_BIN_RECIPE_TYPE.get())) {
            String groupName = recipe.getGroup().trim();
            if (groupName.isEmpty()) {
                continue;
            }

            groups.computeIfAbsent(groupName, ignored -> new SellingBinGroup()).addRecipe(recipe);
        }

        groups.values().removeIf(SellingBinGroup::isEmpty);
        return groups;
    }

    private static boolean applyDailyGroupAdjustments(
            ServerLevel level,
            SellingBinMarketSavedData marketData,
            SellingBinGroup group,
            Map<ResourceLocation, Integer> previousBonuses,
            Map<ResourceLocation, Integer> previousCarryStages
    ) {
        List<SellingBinGroup.Entry> remainingEntries = group.getEntries();
        if (remainingEntries.isEmpty()) {
            return false;
        }

        shuffleEntries(remainingEntries, level.random);

        boolean changed = false;
        int reservedIncreaseSlots = 0;
        int reservedDecreaseSlots = 0;
        for (int i = remainingEntries.size() - 1; i >= 0; i--) {
            SellingBinGroup.Entry entry = remainingEntries.get(i);
            SellingBinRecipe recipe = entry.recipe();
            ResourceLocation priceKey = entry.priceKey();
            int previousBonus = previousBonuses.getOrDefault(priceKey, 0);
            if (previousBonus == 0) {
                continue;
            }

            Integer nextCarryStage = getNextCarryStage(level.random, previousCarryStages.getOrDefault(priceKey, 0));
            if (nextCarryStage == null) {
                continue;
            }

            if (!isLegalPriceBonus(recipe, previousBonus)) {
                continue;
            }

            changed |= applyActivePrice(marketData, priceKey, previousBonus, nextCarryStage);
            remainingEntries.remove(i);
            if (previousBonus > 0) {
                reservedIncreaseSlots++;
            } else {
                reservedDecreaseSlots++;
            }
        }

        int targetIncreaseCount = getRandomInRange(level.random, DAILY_INCREASE_PICK_MIN, DAILY_INCREASE_PICK_MAX);
        int targetDecreaseCount = getRandomInRange(level.random, DAILY_DECREASE_PICK_MIN, DAILY_DECREASE_PICK_MAX);
        int requestedFreshIncreaseCount = Math.max(0, targetIncreaseCount - reservedIncreaseSlots);
        int requestedFreshDecreaseCount = Math.max(0, targetDecreaseCount - reservedDecreaseSlots);

        if (remainingEntries.size() == 1 && requestedFreshIncreaseCount > 0 && requestedFreshDecreaseCount > 0) {
            if (level.random.nextBoolean()) {
                requestedFreshDecreaseCount = 0;
            } else {
                requestedFreshIncreaseCount = 0;
            }
        }

        int increaseCount = Math.min(requestedFreshIncreaseCount, Math.max(0, remainingEntries.size() - requestedFreshDecreaseCount));
        for (int i = 0; i < increaseCount; i++) {
            SellingBinGroup.Entry entry = remainingEntries.remove(remainingEntries.size() - 1);
            int amount = getRandomInRange(level.random, DAILY_INCREASE_AMOUNT_MIN, DAILY_INCREASE_AMOUNT_MAX);
            changed |= applyPriceDelta(marketData, entry, amount);
        }

        int decreaseBudget = Math.min(requestedFreshDecreaseCount, remainingEntries.size());
        for (int i = remainingEntries.size() - 1; i >= 0 && decreaseBudget > 0; i--) {
            SellingBinGroup.Entry entry = remainingEntries.get(i);
            int amount = getRandomInRange(level.random, DAILY_DECREASE_AMOUNT_MIN, DAILY_DECREASE_AMOUNT_MAX);
            if (applyPriceDelta(marketData, entry, -amount)) {
                changed = true;
                decreaseBudget--;
            }
        }

        return changed;
    }

    private static Integer getNextCarryStage(RandomSource random, int previousCarryStage) {
        if (previousCarryStage <= 0) {
            return random.nextFloat() < FIRST_CARRY_CHANCE ? 1 : null;
        }
        if (previousCarryStage == 1) {
            return random.nextFloat() < SECOND_CARRY_CHANCE ? 2 : null;
        }
        return null;
    }

    private static boolean applyActivePrice(SellingBinMarketSavedData marketData, ResourceLocation priceKey, int priceBonus, int carryStage) {
        boolean changed = marketData.setPriceBonus(priceKey, priceBonus);
        changed |= marketData.setCarryStage(priceKey, carryStage);
        return changed;
    }

    private static boolean applyPriceDelta(SellingBinMarketSavedData marketData, SellingBinGroup.Entry entry, int delta) {
        if (delta == 0) {
            return false;
        }

        SellingBinRecipe recipe = entry.recipe();
        ResourceLocation priceKey = entry.priceKey();
        int currentBonus = marketData.getPriceBonus(priceKey);
        if (delta < 0) {
            Integer legalDelta = getLegalDecreaseDelta(recipe, currentBonus, delta);
            if (legalDelta == null) {
                return false;
            }
            delta = legalDelta;
        }

        long nextBonus = (long) currentBonus + delta;
        if (nextBonus >= Integer.MAX_VALUE) {
            return applyActivePrice(marketData, priceKey, Integer.MAX_VALUE, 0);
        }

        return applyActivePrice(marketData, priceKey, (int) nextBonus, 0);
    }

    private static Integer getLegalDecreaseDelta(SellingBinRecipe recipe, int currentBonus, int requestedDelta) {
        if (requestedDelta >= 0) {
            return requestedDelta;
        }

        if (isLegalPriceBonus(recipe, currentBonus + requestedDelta)) {
            return requestedDelta;
        }

        if (requestedDelta != -1 && isLegalPriceBonus(recipe, currentBonus - 1)) {
            return -1;
        }

        return null;
    }

    private static boolean isLegalPriceBonus(SellingBinRecipe recipe, int priceBonus) {
        return recipe.getRawMinOutputCount(priceBonus) >= 1L;
    }

    private static int getRandomInRange(RandomSource random, int min, int max) {
        if (max <= min) {
            return min;
        }

        return min + random.nextInt(max - min + 1);
    }

    private static void shuffleEntries(List<SellingBinGroup.Entry> entries, RandomSource random) {
        for (int i = entries.size() - 1; i > 0; i--) {
            int swapIndex = random.nextInt(i + 1);
            Collections.swap(entries, i, swapIndex);
        }
    }
}

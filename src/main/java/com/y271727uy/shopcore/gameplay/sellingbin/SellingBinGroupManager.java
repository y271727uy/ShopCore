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

    private SellingBinGroupManager() {
    }

    public static int getPriceBonus(ServerLevel level, ResourceLocation recipeId) {
        return SellingBinMarketSavedData.get(level).getPriceBonus(recipeId);
    }

    public static Map<ResourceLocation, Integer> snapshotPriceBonuses(ServerLevel level) {
        return SellingBinMarketSavedData.get(level).snapshotPriceBonuses();
    }

    public static boolean refreshForElapsedDays(ServerLevel level) {
        SellingBinMarketSavedData marketData = SellingBinMarketSavedData.get(level);
        long currentDay = Math.floorDiv(level.getDayTime(), DAY_LENGTH_TICKS);

        if (!marketData.isInitialized()) {
            marketData.setLastProcessedDay(currentDay);
            return false;
        }

        long lastProcessedDay = marketData.getLastProcessedDay();
        if (currentDay <= lastProcessedDay) {
            return false;
        }

        Map<String, SellingBinGroup> groups = collectGroups(level);
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
        ArrayList<SellingBinRecipe> remainingRecipes = new ArrayList<>(group.getRecipes());
        if (remainingRecipes.isEmpty()) {
            return false;
        }

        shuffleRecipes(remainingRecipes, level.random);

        boolean changed = false;
        int reservedIncreaseSlots = 0;
        int reservedDecreaseSlots = 0;
        for (int i = remainingRecipes.size() - 1; i >= 0; i--) {
            SellingBinRecipe recipe = remainingRecipes.get(i);
            ResourceLocation recipeId = recipe.getId();
            int previousBonus = previousBonuses.getOrDefault(recipeId, 0);
            if (previousBonus == 0) {
                continue;
            }

            Integer nextCarryStage = getNextCarryStage(level.random, previousCarryStages.getOrDefault(recipeId, 0));
            if (nextCarryStage == null) {
                continue;
            }

            if (!isLegalPriceBonus(recipe, previousBonus)) {
                continue;
            }

            changed |= applyActivePrice(marketData, recipeId, previousBonus, nextCarryStage);
            remainingRecipes.remove(i);
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

        if (remainingRecipes.size() == 1 && requestedFreshIncreaseCount > 0 && requestedFreshDecreaseCount > 0) {
            if (level.random.nextBoolean()) {
                requestedFreshDecreaseCount = 0;
            } else {
                requestedFreshIncreaseCount = 0;
            }
        }

        int increaseCount = Math.min(requestedFreshIncreaseCount, Math.max(0, remainingRecipes.size() - requestedFreshDecreaseCount));
        for (int i = 0; i < increaseCount; i++) {
            SellingBinRecipe recipe = remainingRecipes.remove(remainingRecipes.size() - 1);
            int amount = getRandomInRange(level.random, DAILY_INCREASE_AMOUNT_MIN, DAILY_INCREASE_AMOUNT_MAX);
            changed |= applyPriceDelta(marketData, recipe, amount, 0);
        }

        int decreaseBudget = Math.min(requestedFreshDecreaseCount, remainingRecipes.size());
        for (int i = remainingRecipes.size() - 1; i >= 0 && decreaseBudget > 0; i--) {
            SellingBinRecipe recipe = remainingRecipes.get(i);
            int amount = getRandomInRange(level.random, DAILY_DECREASE_AMOUNT_MIN, DAILY_DECREASE_AMOUNT_MAX);
            if (applyPriceDelta(marketData, recipe, -amount, 0)) {
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

    private static boolean applyActivePrice(SellingBinMarketSavedData marketData, ResourceLocation recipeId, int priceBonus, int carryStage) {
        boolean changed = marketData.setPriceBonus(recipeId, priceBonus);
        changed |= marketData.setCarryStage(recipeId, carryStage);
        return changed;
    }

    private static boolean applyPriceDelta(SellingBinMarketSavedData marketData, SellingBinRecipe recipe, int delta, int carryStage) {
        if (delta == 0) {
            return false;
        }

        ResourceLocation recipeId = recipe.getId();
        int currentBonus = marketData.getPriceBonus(recipeId);
        if (delta < 0) {
            Integer legalDelta = getLegalDecreaseDelta(recipe, currentBonus, delta);
            if (legalDelta == null) {
                return false;
            }
            delta = legalDelta;
        }

        long nextBonus = (long) currentBonus + delta;
        if (nextBonus >= Integer.MAX_VALUE) {
            return applyActivePrice(marketData, recipeId, Integer.MAX_VALUE, carryStage);
        }

        return applyActivePrice(marketData, recipeId, (int) nextBonus, carryStage);
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

    private static void shuffleRecipes(List<SellingBinRecipe> recipes, RandomSource random) {
        for (int i = recipes.size() - 1; i > 0; i--) {
            int swapIndex = random.nextInt(i + 1);
            Collections.swap(recipes, i, swapIndex);
        }
    }
}

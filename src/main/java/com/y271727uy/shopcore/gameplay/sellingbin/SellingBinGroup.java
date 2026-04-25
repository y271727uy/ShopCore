package com.y271727uy.shopcore.gameplay.sellingbin;

import com.y271727uy.shopcore.recipe.SellingBinRecipe;

import java.util.ArrayList;
import java.util.List;

public final class SellingBinGroup {
    private final List<Entry> entries = new ArrayList<>();

    public SellingBinGroup() {
    }

    public void addRecipe(SellingBinRecipe recipe) {
        for (var matchingItem : recipe.getInputChoices()) {
            if (matchingItem.isEmpty()) {
                continue;
            }
            entries.add(new Entry(recipe));
            break;
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public List<SellingBinRecipe> getRecipes() {
        List<SellingBinRecipe> recipes = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            recipes.add(entry.recipe());
        }
        return recipes;
    }

    private record Entry(SellingBinRecipe recipe) {
    }
}


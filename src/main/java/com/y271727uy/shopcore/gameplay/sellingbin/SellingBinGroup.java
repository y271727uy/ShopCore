package com.y271727uy.shopcore.gameplay.sellingbin;

import com.y271727uy.shopcore.recipe.SellingBinRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SellingBinGroup {
    private final List<Entry> entries = new ArrayList<>();

    public SellingBinGroup() {
    }

    public void addRecipe(SellingBinRecipe recipe) {
        for (ItemStack matchingItem : recipe.getInputChoices()) {
            if (matchingItem.isEmpty()) {
                continue;
            }
            entries.add(new Entry(recipe, recipe.getPriceKey(matchingItem)));
        }
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public List<Entry> getEntries() {
        List<Entry> recipes = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            recipes.add(entry);
        }
        return recipes;
    }

    public record Entry(SellingBinRecipe recipe, ResourceLocation priceKey) {
    }
}


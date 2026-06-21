package com.y271727uy.shopcore.economic.micromachinelearning.weight;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Reads shopcore:weight_one through shopcore:weight_ten item tags.
 */
public final class ItemTagWeightAmountPolicy implements WeightAmountPolicy<ItemStack> {
    public static final ItemTagWeightAmountPolicy DEFAULT = new ItemTagWeightAmountPolicy(1.0D);

    private final double fallbackUnitAmount;

    public ItemTagWeightAmountPolicy(double fallbackUnitAmount) {
        if (!Double.isFinite(fallbackUnitAmount) || fallbackUnitAmount < 0.0D) {
            throw new IllegalArgumentException("fallbackUnitAmount must be a finite non-negative value");
        }
        this.fallbackUnitAmount = fallbackUnitAmount;
    }

    /**
     * Returns the tag-defined weight for one item unit.
     */
    public double unitAmountOf(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) {
            return 0.0D;
        }

        for (int index = WeightTags.ORDERED.size() - 1; index >= 0; index--) {
            TagKey<Item> tag = WeightTags.ORDERED.get(index);
            if (stack.is(tag)) {
                return index + 1.0D;
            }
        }
        return fallbackUnitAmount;
    }

    /**
     * Returns the weight increment for the whole stack.
     */
    @Override
    public double amountOf(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty()) {
            return 0.0D;
        }
        return unitAmountOf(stack) * stack.getCount();
    }
}

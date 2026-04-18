package com.list.recipe;

import net.minecraft.world.item.ItemStack;

public record ChancedItemStack(ItemStack itemStack, float chance) {

    public ChancedItemStack(ItemStack itemStack) {
        this(itemStack, 1.0f);
    }
}

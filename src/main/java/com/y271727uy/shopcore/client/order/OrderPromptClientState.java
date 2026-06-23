package com.y271727uy.shopcore.client.order;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class OrderPromptClientState {
    private static List<ItemStack> displayStacks = List.of();

    private OrderPromptClientState() {
    }

    public static void show(List<ItemStack> stacks) {
        displayStacks = List.copyOf(stacks.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .limit(15)
                .toList());
    }

    public static void clear() {
        displayStacks = List.of();
    }

    public static List<ItemStack> displayStacks() {
        return displayStacks;
    }
}

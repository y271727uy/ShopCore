package com.y271727uy.shopcore.core.order.prompt;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record OrderPromptLine(
        ItemStack displayStack,
        int count,
        String text,
        boolean completed
) {
    public OrderPromptLine {
        displayStack = Objects.requireNonNull(displayStack, "displayStack").copy();
        if (displayStack.isEmpty()) {
            throw new IllegalArgumentException("displayStack cannot be empty");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }
        text = Objects.requireNonNullElse(text, "");
    }
}

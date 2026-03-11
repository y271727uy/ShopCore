package com.y271727uy.shopcore.economic;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A single price rule used to match an item stack and provide a {@link Price}.
 */
public final class PriceDefinition {
    private final Predicate<ItemStack> matcher;
    private final Price price;
    private final String description;
    private final int priority;

    private PriceDefinition(Predicate<ItemStack> matcher, Price price, String description, int priority) {
        this.matcher = matcher;
        this.price = price;
        this.description = description;
        this.priority = priority;
    }

    public static PriceDefinition forItem(Item item, Price price) {
        Objects.requireNonNull(item, "item");
        return new PriceDefinition(stack -> stack.is(item), price, "item:" + item, 200);
    }

    public static PriceDefinition forTag(TagKey<Item> tag, Price price) {
        Objects.requireNonNull(tag, "tag");
        return new PriceDefinition(stack -> stack.is(tag), price, "tag:" + tag.location(), 100);
    }

    public static PriceDefinition custom(Predicate<ItemStack> matcher, Price price, String description, int priority) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(description, "description");
        return new PriceDefinition(matcher, price, description, priority);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && matcher.test(stack);
    }

    public Price price() {
        return price;
    }

    public String description() {
        return description;
    }

    public int priority() {
        return priority;
    }
}


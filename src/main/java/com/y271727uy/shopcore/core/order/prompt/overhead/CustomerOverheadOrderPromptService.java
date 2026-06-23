package com.y271727uy.shopcore.core.order.prompt.overhead;

import com.y271727uy.shopcore.core.order.prompt.OrderPrompt;
import com.y271727uy.shopcore.core.order.prompt.OrderPromptLine;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CustomerOverheadOrderPromptService {
    private static final String ROOT_TAG = "ShopCoreOverheadOrderPrompt";
    private static final String BACKGROUND_ID_TAG = "Background";
    private static final String ORDER_ID_TAG = "Order";
    private static final String TEXT_ID_TAG = "Text";
    private static final ItemStack WAITING_ICON = new ItemStack(Items.COCOA_BEANS);

    private CustomerOverheadOrderPromptService() {
    }

    public static void show(Entity customer, OrderPrompt prompt) {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(prompt, "prompt");
        if (!(customer.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 basePosition = displayBasePosition(customer);
        clearDisplayEntity(customer, BACKGROUND_ID_TAG);
        Display.ItemDisplay order = getOrCreateItemDisplay(level, customer, ORDER_ID_TAG, basePosition);
        Display.TextDisplay text = getOrCreateTextDisplay(level, customer, TEXT_ID_TAG, basePosition.add(0.0D, 0.5D, 0.0D));

        ItemStack orderIcon = primaryOrderIcon(prompt).orElse(ItemStack.EMPTY);
        configureOrderItem(order, basePosition, orderIcon);
        configureText(text, basePosition.add(0.0D, 0.5D, 0.0D), Component.literal(overheadText(prompt)));
    }

    public static void clear(Entity customer) {
        Objects.requireNonNull(customer, "customer");
        CompoundTag tag = customer.getPersistentData().getCompound(ROOT_TAG);
        discardStoredEntity(customer, tag, BACKGROUND_ID_TAG);
        discardStoredEntity(customer, tag, ORDER_ID_TAG);
        discardStoredEntity(customer, tag, TEXT_ID_TAG);
        customer.getPersistentData().remove(ROOT_TAG);
    }

    public static void showWaiting(Entity customer, Component text) {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(text, "text");
        if (!(customer.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 basePosition = displayBasePosition(customer);
        clearDisplayEntity(customer, BACKGROUND_ID_TAG);
        Display.ItemDisplay order = getOrCreateItemDisplay(level, customer, ORDER_ID_TAG, basePosition);
        Display.TextDisplay textDisplay = getOrCreateTextDisplay(level, customer, TEXT_ID_TAG, basePosition.add(0.0D, 0.5D, 0.0D));
        configureOrderItem(order, basePosition, WAITING_ICON);
        configureText(textDisplay, basePosition.add(0.0D, 0.5D, 0.0D), text);
    }

    private static String overheadText(OrderPrompt prompt) {
        return prompt.lines().stream()
                .limit(3)
                .map(line -> line.count() + "x " + line.text())
                .collect(Collectors.joining(" | "));
    }

    private static Optional<ItemStack> primaryOrderIcon(OrderPrompt prompt) {
        return prompt.lines().stream()
                .findFirst()
                .map(OrderPromptLine::displayStack)
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(1));
    }

    private static Vec3 displayBasePosition(Entity customer) {
        return customer.position().add(0.0D, customer.getBbHeight() + 0.55D, 0.0D);
    }

    private static Display.ItemDisplay getOrCreateItemDisplay(ServerLevel level, Entity customer, String tagName, Vec3 position) {
        Optional<Entity> existing = storedEntity(customer, tagName);
        if (existing.orElse(null) instanceof Display.ItemDisplay display) {
            display.setPos(position);
            return display;
        }

        Display.ItemDisplay display = EntityType.ITEM_DISPLAY.create(level);
        if (display == null) {
            throw new IllegalStateException("failed to create item display");
        }
        display.setPos(position);
        display.setNoGravity(true);
        display.setInvulnerable(true);
        display.setSilent(true);
        level.addFreshEntity(display);
        storeEntity(customer, tagName, display.getUUID());
        return display;
    }

    private static Display.TextDisplay getOrCreateTextDisplay(ServerLevel level, Entity customer, String tagName, Vec3 position) {
        Optional<Entity> existing = storedEntity(customer, tagName);
        if (existing.orElse(null) instanceof Display.TextDisplay display) {
            display.setPos(position);
            return display;
        }

        Display.TextDisplay display = EntityType.TEXT_DISPLAY.create(level);
        if (display == null) {
            throw new IllegalStateException("failed to create text display");
        }
        display.setPos(position);
        display.setNoGravity(true);
        display.setInvulnerable(true);
        display.setSilent(true);
        level.addFreshEntity(display);
        storeEntity(customer, tagName, display.getUUID());
        return display;
    }

    private static void configureOrderItem(Display.ItemDisplay display, Vec3 position, ItemStack stack) {
        CompoundTag tag = baseDisplayTag(position);
        tag.putString("billboard", "vertical");
        tag.put("item", itemTag(stack.isEmpty() ? new ItemStack(Items.BARRIER) : stack));
        tag.putString("item_display", "none");
        tag.put("transformation", transformation(0.8F, 0.8F, 1.0F));
        tag.putInt("interpolation_duration", 5);
        tag.putInt("start_interpolation", 0);
        display.load(display.saveWithoutId(new CompoundTag()).merge(tag));
    }

    private static void configureText(Display.TextDisplay display, Vec3 position, Component text) {
        CompoundTag tag = baseDisplayTag(position);
        tag.putString("billboard", "vertical");
        tag.putString("text", Component.Serializer.toJson(text));
        tag.putByte("shadow", (byte) 1);
        tag.putByte("text_opacity", (byte) -1);
        tag.putInt("background", 0);
        tag.putString("alignment", "center");
        tag.putInt("line_width", 210);
        tag.putBoolean("default_background", false);
        tag.put("transformation", transformation(1.0F, 1.0F, 1.0F));
        tag.putInt("interpolation_duration", 5);
        tag.putInt("start_interpolation", 0);
        display.load(display.saveWithoutId(new CompoundTag()).merge(tag));
    }

    private static CompoundTag baseDisplayTag(Vec3 position) {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    private static ListTag transformation(float scaleX, float scaleY, float scaleZ) {
        ListTag matrix = new ListTag();
        float[] values = {
                scaleX, 0.0F, 0.0F, 0.0F,
                0.0F, scaleY, 0.0F, 0.0F,
                0.0F, 0.0F, scaleZ, 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F
        };
        for (float value : values) {
            matrix.add(FloatTag.valueOf(value));
        }
        return matrix;
    }

    private static CompoundTag itemTag(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        tag.putString("id", itemKey.toString());
        tag.putByte("Count", (byte) Math.max(1, stack.getCount()));
        if (stack.hasTag()) {
            tag.put("tag", stack.getTag().copy());
        }
        return tag;
    }

    private static Optional<Entity> storedEntity(Entity customer, String tagName) {
        CompoundTag tag = customer.getPersistentData().getCompound(ROOT_TAG);
        if (!tag.hasUUID(tagName)) {
            return Optional.empty();
        }
        if (!(customer.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        return Optional.ofNullable(level.getEntity(tag.getUUID(tagName)));
    }

    private static void storeEntity(Entity customer, String tagName, UUID entityId) {
        CompoundTag tag = customer.getPersistentData().getCompound(ROOT_TAG);
        tag.putUUID(tagName, entityId);
        customer.getPersistentData().put(ROOT_TAG, tag);
    }

    private static void discardStoredEntity(Entity customer, CompoundTag tag, String tagName) {
        if (!tag.hasUUID(tagName) || !(customer.level() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = level.getEntity(tag.getUUID(tagName));
        if (entity != null) {
            entity.discard();
        }
    }

    private static void clearDisplayEntity(Entity customer, String tagName) {
        CompoundTag tag = customer.getPersistentData().getCompound(ROOT_TAG);
        discardStoredEntity(customer, tag, tagName);
        tag.remove(tagName);
        customer.getPersistentData().put(ROOT_TAG, tag);
    }
}

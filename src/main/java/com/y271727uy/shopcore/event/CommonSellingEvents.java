package com.y271727uy.shopcore.event;

import com.y271727uy.shopcore.economic.micromachinelearning.helper.PriceAdjustmentResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Common selling hook for modules that settle player delivery, NPC payment, or other sell-like trades.
 * <p>
 * Callers post this before paying the player, then read {@link #getAdjustedSellPrice()}.
 */
public class CommonSellingEvents extends Event {
    private final Player player;
    private final ItemStack soldStack;
    private final ResourceLocation itemKey;
    private final int quantity;
    private final int baseSellPrice;
    private final long nowTick;
    @Nullable
    private final BlockPos deliveryPos;
    @Nullable
    private final Object source;

    private int adjustedSellPrice;
    private double multiplier;
    @Nullable
    private PriceAdjustmentResult priceAdjustmentResult;

    public CommonSellingEvents(Player player, ItemStack soldStack, int quantity, int baseSellPrice) {
        this(player, soldStack, quantity, baseSellPrice, player.level().getGameTime(), null, null, null);
    }

    public CommonSellingEvents(
            Player player,
            ItemStack soldStack,
            int quantity,
            int baseSellPrice,
            long nowTick,
            @Nullable BlockPos deliveryPos,
            @Nullable Object source
    ) {
        this(player, soldStack, quantity, baseSellPrice, nowTick, deliveryPos, source, null);
    }

    public CommonSellingEvents(
            Player player,
            ItemStack soldStack,
            int quantity,
            int baseSellPrice,
            long nowTick,
            @Nullable BlockPos deliveryPos,
            @Nullable Object source,
            @Nullable ResourceLocation itemKey
    ) {
        this.player = Objects.requireNonNull(player, "player");
        Objects.requireNonNull(soldStack, "soldStack");
        if (soldStack.isEmpty()) {
            throw new IllegalArgumentException("soldStack cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (baseSellPrice < 0) {
            throw new IllegalArgumentException("baseSellPrice cannot be negative");
        }

        this.soldStack = soldStack.copy();
        this.quantity = quantity;
        this.baseSellPrice = baseSellPrice;
        this.adjustedSellPrice = baseSellPrice;
        this.multiplier = 1.0D;
        this.nowTick = nowTick;
        this.deliveryPos = deliveryPos == null ? null : deliveryPos.immutable();
        this.source = source;
        this.itemKey = itemKey == null ? BuiltInRegistries.ITEM.getKey(soldStack.getItem()) : itemKey;
    }

    public static CommonSellingEvents post(Player player, ItemStack soldStack, int quantity, int baseSellPrice) {
        CommonSellingEvents event = new CommonSellingEvents(player, soldStack, quantity, baseSellPrice);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static CommonSellingEvents post(
            Player player,
            ItemStack soldStack,
            int quantity,
            int baseSellPrice,
            long nowTick,
            @Nullable BlockPos deliveryPos,
            @Nullable Object source
    ) {
        CommonSellingEvents event = new CommonSellingEvents(player, soldStack, quantity, baseSellPrice, nowTick, deliveryPos, source);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getSoldStack() {
        return soldStack.copy();
    }

    public ResourceLocation getItemKey() {
        return itemKey;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getBaseSellPrice() {
        return baseSellPrice;
    }

    public int getAdjustedSellPrice() {
        return adjustedSellPrice;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getNowTick() {
        return nowTick;
    }

    @Nullable
    public BlockPos getDeliveryPos() {
        return deliveryPos;
    }

    @Nullable
    public Object getSource() {
        return source;
    }

    @Nullable
    public PriceAdjustmentResult getPriceAdjustmentResult() {
        return priceAdjustmentResult;
    }

    public void setAdjustedSellPrice(int adjustedSellPrice) {
        if (adjustedSellPrice < 0) {
            throw new IllegalArgumentException("adjustedSellPrice cannot be negative");
        }
        this.adjustedSellPrice = adjustedSellPrice;
        this.multiplier = baseSellPrice <= 0 ? 1.0D : (double) adjustedSellPrice / (double) baseSellPrice;
        this.priceAdjustmentResult = null;
    }

    public void applyPriceAdjustment(PriceAdjustmentResult result) {
        Objects.requireNonNull(result, "result");
        this.adjustedSellPrice = result.adjustedSellPrice();
        this.multiplier = result.multiplier();
        this.priceAdjustmentResult = result;
    }
}

package com.y271727uy.shopcore.integration.jei.sdmshop;

import com.y271727uy.shopcore.economic.Price;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record SdmShopJeiEntry(ItemStack itemStack, Price price, int quantity, String shopName, String currency, boolean sell, boolean locked, String lockReason) {
    public SdmShopJeiEntry {
        Objects.requireNonNull(itemStack, "itemStack");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(shopName, "shopName");
        Objects.requireNonNull(currency, "currency");
        Objects.requireNonNull(lockReason, "lockReason");

        itemStack = itemStack.copy();
        if (!itemStack.isEmpty()) {
            itemStack.setCount(Math.max(1, quantity));
        }

        quantity = Math.max(1, quantity);
    }

    public ItemStack displayStack() {
        ItemStack copy = itemStack.copy();
        copy.setCount(Math.max(1, quantity));
        return copy;
    }

    public String priceText() {
        int total = Math.max(0, price.totalPrice());
        return Integer.toString(total);
    }

    public String displayNameKey() {
        return itemStack.getDescriptionId();
    }

    public boolean isSell() {
        return sell;
    }

    public static SdmShopJeiEntry from(Object shopTab, Object entry) {
        Objects.requireNonNull(shopTab, "shopTab");
        Objects.requireNonNull(entry, "entry");

        Object nestedEntry = SdmShopDataBridge.invokeFirst(entry, "getEntry", "entry", "getShopEntry", "shopEntry")
                .orElse(entry);
        Object nestedTab = SdmShopDataBridge.invokeFirst(shopTab, "getShopTab", "shopTab", "tab")
                .orElse(shopTab);

        ItemStack stack = SdmShopDataBridge.extractItemStackValue(nestedEntry)
                .or(() -> SdmShopDataBridge.extractItemStackValue(entry))
                .orElse(ItemStack.EMPTY);
        Price price = SdmShopDataBridge.extractPriceValue(nestedEntry)
                .or(() -> SdmShopDataBridge.extractPriceValue(entry))
                .orElse(Price.EMPTY);
        int quantity = SdmShopDataBridge.extractIntValue(nestedEntry, "getQuantity", "quantity", "getCount", "count", "getAmount", "amount")
                .or(() -> SdmShopDataBridge.extractIntValue(entry, "getQuantity", "quantity", "getCount", "count", "getAmount", "amount"))
                .orElse(stack.isEmpty() ? 1 : stack.getCount());
        String shopName = SdmShopDataBridge.extractStringValue(nestedTab, "getTitle", "title", "getName", "name")
                .or(() -> SdmShopDataBridge.extractStringValue(shopTab, "getTitle", "title", "getName", "name"))
                .or(() -> SdmShopDataBridge.extractStringValue(nestedEntry, "getTitle", "title", "getName", "name"))
                .orElseGet(() -> stack.isEmpty() ? "SDM Shop" : stack.getDescriptionId());
        boolean locked = SdmShopDataBridge.extractBooleanValue(nestedEntry, "isLock", "isLocked", "locked", "hasLock")
                .or(() -> SdmShopDataBridge.extractBooleanValue(entry, "isLock", "isLocked", "locked", "hasLock"))
                .orElse(false);
        boolean sell = SdmShopDataBridge.extractBooleanValue(nestedEntry, "isSell", "sell")
                .or(() -> SdmShopDataBridge.extractBooleanValue(entry, "isSell", "sell"))
                .orElse(false);
        String lockReason = locked
                ? SdmShopDataBridge.extractStringValue(nestedEntry, "getQuestTitle", "getLockedReason", "getLockReason", "getReason")
                    .or(() -> SdmShopDataBridge.extractStringValue(entry, "getQuestTitle", "getLockedReason", "getLockReason", "getReason"))
                    .orElse("")
                : "";

        return new SdmShopJeiEntry(stack, price, quantity, shopName, SdmShopDataBridge.DEFAULT_CURRENCY, sell, locked, lockReason);
    }
}



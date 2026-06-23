package com.y271727uy.shopcore.core.order.storage;

import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import com.y271727uy.shopcore.core.order.PricingMode;
import com.y271727uy.shopcore.core.order.ShopListing;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class ShopListingNbt {
    private static final String SLOT_INDEX = "SlotIndex";
    private static final String MENU_ID = "MenuId";
    private static final String DEMAND_CATEGORY = "DemandCategory";
    private static final String COMPLEXITY = "Complexity";
    private static final String ENABLED = "Enabled";
    private static final String PRICING_MODE = "PricingMode";
    private static final String MANUAL_UNIT_PRICE = "ManualUnitPrice";
    private static final String MAX_PER_ORDER = "MaxPerOrder";
    private static final String STOCK_COUNT = "StockCount";

    private ShopListingNbt() {
    }

    public static CompoundTag save(ShopListing listing) {
        Objects.requireNonNull(listing, "listing");
        CompoundTag tag = new CompoundTag();
        tag.putInt(SLOT_INDEX, listing.slotIndex());
        tag.putString(MENU_ID, listing.menuId());
        tag.putString(DEMAND_CATEGORY, listing.demandCategory().toString());
        tag.putString(COMPLEXITY, listing.complexity().name());
        tag.putBoolean(ENABLED, listing.enabled());
        tag.putString(PRICING_MODE, listing.pricingMode().name());
        tag.putInt(MANUAL_UNIT_PRICE, listing.manualUnitPrice());
        tag.putInt(MAX_PER_ORDER, listing.maxPerOrder());
        tag.putInt(STOCK_COUNT, listing.stockCount());
        return tag;
    }

    public static ShopListing load(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        ResourceLocation demandCategory = ResourceLocation.tryParse(tag.getString(DEMAND_CATEGORY));
        if (demandCategory == null) {
            demandCategory = ResourceLocation.fromNamespaceAndPath("shopcore", "unknown");
        }
        return new ShopListing(
                Math.max(0, tag.getInt(SLOT_INDEX)),
                tag.getString(MENU_ID),
                demandCategory,
                enumValue(tag.getString(COMPLEXITY), OrderComplexity.SINGLE_ITEM),
                !tag.contains(ENABLED) || tag.getBoolean(ENABLED),
                enumValue(tag.getString(PRICING_MODE), PricingMode.AUTO),
                Math.max(0, tag.getInt(MANUAL_UNIT_PRICE)),
                Math.max(1, tag.getInt(MAX_PER_ORDER)),
                tag.contains(STOCK_COUNT) ? Math.max(ShopListing.UNKNOWN_STOCK, tag.getInt(STOCK_COUNT)) : ShopListing.UNKNOWN_STOCK
        );
    }

    private static <T extends Enum<T>> T enumValue(String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(fallback.getDeclaringClass(), value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}

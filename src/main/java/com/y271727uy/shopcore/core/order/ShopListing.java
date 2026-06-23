package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * One concrete supply slot configured by a shop.
 * The menu id resolves the tag-driven item pool; the demand category connects it to market/customer demand.
 */
public record ShopListing(
        int slotIndex,
        String menuId,
        ResourceLocation demandCategory,
        OrderComplexity complexity,
        boolean enabled,
        PricingMode pricingMode,
        int manualUnitPrice,
        int maxPerOrder,
        int stockCount
) {
    public static final int UNKNOWN_STOCK = -1;

    public ShopListing {
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex cannot be negative");
        }
        menuId = normalizeMenuId(menuId);
        Objects.requireNonNull(demandCategory, "demandCategory");
        Objects.requireNonNull(complexity, "complexity");
        Objects.requireNonNull(pricingMode, "pricingMode");
        if (manualUnitPrice < 0) {
            throw new IllegalArgumentException("manualUnitPrice cannot be negative");
        }
        if (maxPerOrder < 1) {
            throw new IllegalArgumentException("maxPerOrder must be at least 1");
        }
        if (stockCount < UNKNOWN_STOCK) {
            throw new IllegalArgumentException("stockCount cannot be less than UNKNOWN_STOCK");
        }
    }

    public static ShopListing auto(
            int slotIndex,
            String menuId,
            DemandCategoryKey demandCategory,
            OrderComplexity complexity,
            int maxPerOrder,
            int stockCount
    ) {
        return new ShopListing(
                slotIndex,
                menuId,
                demandCategory.id(),
                complexity,
                true,
                PricingMode.AUTO,
                0,
                maxPerOrder,
                stockCount
        );
    }

    public static ShopListing manual(
            int slotIndex,
            String menuId,
            DemandCategoryKey demandCategory,
            OrderComplexity complexity,
            int manualUnitPrice,
            int maxPerOrder,
            int stockCount
    ) {
        return new ShopListing(
                slotIndex,
                menuId,
                demandCategory.id(),
                complexity,
                true,
                PricingMode.MANUAL,
                manualUnitPrice,
                maxPerOrder,
                stockCount
        );
    }

    public boolean hasKnownStock() {
        return stockCount != UNKNOWN_STOCK;
    }

    public int availableForOrder() {
        if (!hasKnownStock()) {
            return maxPerOrder;
        }
        return Math.min(maxPerOrder, stockCount);
    }

    private static String normalizeMenuId(String menuId) {
        String normalized = Objects.requireNonNull(menuId, "menuId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("menuId cannot be blank");
        }
        return normalized;
    }
}

package com.y271727uy.shopcore.economic.pricesetting;

import com.y271727uy.shopcore.economic.price.Price;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Hand-maintained DSL output for one authoritative price declaration.
 */
public record PriceSettingDefinition(
        String itemIdOrTagId,
        Price price,
        Optional<String> tooltipTitleKey,
        List<String> menuIds
) {
    public PriceSettingDefinition {
        itemIdOrTagId = normalizeItemReference(itemIdOrTagId);
        Objects.requireNonNull(price, "price");
        tooltipTitleKey = tooltipTitleKey == null
                ? Optional.empty()
                : tooltipTitleKey.map(PriceSettingDefinition::normalizeTranslationKey);
        menuIds = List.copyOf(Objects.requireNonNull(menuIds, "menuIds").stream()
                .map(PriceSettingDefinition::normalizeMenuId)
                .distinct()
                .toList());
    }

    public static Builder item(String itemId) {
        return new Builder(itemId);
    }

    public static Builder tag(String tagId) {
        String normalized = normalizeItemReference(tagId);
        return new Builder(normalized.startsWith("#") ? normalized : "#" + normalized);
    }

    public boolean tagReference() {
        return itemIdOrTagId.startsWith("#");
    }

    private static String normalizeItemReference(String itemIdOrTagId) {
        String normalized = Objects.requireNonNull(itemIdOrTagId, "itemIdOrTagId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("itemIdOrTagId cannot be blank");
        }
        return normalized;
    }

    static String normalizeMenuId(String menuId) {
        String normalized = Objects.requireNonNull(menuId, "menuId").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("menuId cannot be blank");
        }
        return normalized;
    }

    private static String normalizeTranslationKey(String translationKey) {
        String normalized = Objects.requireNonNull(translationKey, "translationKey").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("translationKey cannot be blank");
        }
        return normalized;
    }

    public static final class Builder {
        private final String itemIdOrTagId;
        private Price price;
        private String tooltipTitleKey;
        private final List<String> menuIds = new ArrayList<>();

        private Builder(String itemIdOrTagId) {
            this.itemIdOrTagId = normalizeItemReference(itemIdOrTagId);
        }

        public Builder price(int basicPrice, int addPrice, int reputation) {
            this.price = Price.of(basicPrice, addPrice, reputation);
            return this;
        }

        public Builder price(Price price) {
            this.price = Objects.requireNonNull(price, "price");
            return this;
        }

        public Builder tooltipTitle(String translationKey) {
            this.tooltipTitleKey = normalizeTranslationKey(translationKey);
            return this;
        }

        public Builder menu(String menuId) {
            this.menuIds.add(normalizeMenuId(menuId));
            return this;
        }

        public Builder menus(String... menuIds) {
            Objects.requireNonNull(menuIds, "menuIds");
            for (String menuId : menuIds) {
                menu(menuId);
            }
            return this;
        }

        public PriceSettingDefinition build() {
            if (price == null) {
                throw new IllegalStateException("price must be configured for " + itemIdOrTagId);
            }
            return new PriceSettingDefinition(
                    itemIdOrTagId,
                    price,
                    Optional.ofNullable(tooltipTitleKey),
                    menuIds
            );
        }
    }
}

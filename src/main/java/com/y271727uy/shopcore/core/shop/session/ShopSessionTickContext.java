package com.y271727uy.shopcore.core.shop.session;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRuleSet;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyContext;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ShopSessionTickContext(
        ShopInstance shop,
        ShopOrderBook orderBook,
        List<ShopListing> listings,
        Map<ResourceLocation, Object> openingAttributes,
        Optional<ShopOpeningRuleSet> openingRuleSet,
        Optional<ShopOrder> incomingOrder,
        boolean manualOpen,
        long dayTime,
        long gameTime
) {
    public ShopSessionTickContext {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(orderBook, "orderBook");
        if (!shop.shopId().equals(orderBook.shopId())) {
            throw new IllegalArgumentException("orderBook belongs to another shop");
        }
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
        openingAttributes = Map.copyOf(Objects.requireNonNull(openingAttributes, "openingAttributes"));
        openingRuleSet = Objects.requireNonNullElse(openingRuleSet, Optional.empty());
        incomingOrder = Objects.requireNonNullElse(incomingOrder, Optional.empty());
        if (dayTime < 0L) {
            throw new IllegalArgumentException("dayTime cannot be negative");
        }
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
    }

    public OperatingPolicyContext operatingPolicyContext() {
        return new OperatingPolicyContext(shop.operatingPolicy(), dayTime, manualOpen);
    }
}

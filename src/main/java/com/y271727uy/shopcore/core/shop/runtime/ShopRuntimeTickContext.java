package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.OrderSelectionContext;
import com.y271727uy.shopcore.core.order.customer.CustomerProfileSelector;
import com.y271727uy.shopcore.core.order.generation.OrderGenerationSchedule;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRuleSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ShopRuntimeTickContext(
        ShopInstance shop,
        ShopMenuSnapshot menuSnapshot,
        DemandPoolCatalog demandPoolCatalog,
        ShopOpeningRuleSet openingRuleSet,
        ShopOrderBook orderBook,
        Optional<CustomerProfile> customerProfile,
        Optional<CustomerProfileSelector> customerProfileSelector,
        OrderGenerationSchedule orderGenerationSchedule,
        Map<ResourceLocation, Object> openingAttributes,
        boolean openRequested,
        boolean orderGenerationRequested,
        double reputation,
        long dayTime,
        long gameTime,
        long orderTtlTicks,
        RandomSource random
) {
    public ShopRuntimeTickContext {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(menuSnapshot, "menuSnapshot");
        Objects.requireNonNull(demandPoolCatalog, "demandPoolCatalog");
        Objects.requireNonNull(openingRuleSet, "openingRuleSet");
        Objects.requireNonNull(orderBook, "orderBook");
        customerProfile = Objects.requireNonNullElse(customerProfile, Optional.empty());
        customerProfileSelector = Objects.requireNonNullElse(customerProfileSelector, Optional.empty());
        orderGenerationSchedule = Objects.requireNonNullElse(orderGenerationSchedule, OrderGenerationSchedule.ALWAYS);
        openingAttributes = Map.copyOf(Objects.requireNonNull(openingAttributes, "openingAttributes"));
        if (!shop.shopId().equals(menuSnapshot.shopId())) {
            throw new IllegalArgumentException("menu snapshot belongs to another shop");
        }
        if (!shop.shopId().equals(orderBook.shopId())) {
            throw new IllegalArgumentException("order book belongs to another shop");
        }
        if (Double.isNaN(reputation)) {
            reputation = 0.0D;
        }
        if (dayTime < 0L) {
            throw new IllegalArgumentException("dayTime cannot be negative");
        }
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        if (orderTtlTicks < 0L) {
            orderTtlTicks = OrderSelectionContext.DEFAULT_ORDER_TTL_TICKS;
        }
        Objects.requireNonNull(random, "random");
    }
}

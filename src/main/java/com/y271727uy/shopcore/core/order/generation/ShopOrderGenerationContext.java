package com.y271727uy.shopcore.core.order.generation;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.OrderSelectionContext;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import net.minecraft.util.RandomSource;

import java.util.Objects;

public record ShopOrderGenerationContext(
        ShopInstance shop,
        ShopMenuSnapshot menuSnapshot,
        DemandPoolCatalog demandPoolCatalog,
        ShopOrderBook orderBook,
        CustomerProfile customerProfile,
        double reputation,
        boolean generationRequested,
        long gameTime,
        long orderTtlTicks,
        RandomSource random
) {
    public ShopOrderGenerationContext {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(menuSnapshot, "menuSnapshot");
        Objects.requireNonNull(demandPoolCatalog, "demandPoolCatalog");
        Objects.requireNonNull(orderBook, "orderBook");
        Objects.requireNonNull(customerProfile, "customerProfile");
        if (!shop.shopId().equals(menuSnapshot.shopId())) {
            throw new IllegalArgumentException("menu snapshot belongs to another shop");
        }
        if (!shop.shopId().equals(orderBook.shopId())) {
            throw new IllegalArgumentException("order book belongs to another shop");
        }
        if (Double.isNaN(reputation)) {
            reputation = 0.0D;
        }
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        if (orderTtlTicks < 0L) {
            throw new IllegalArgumentException("orderTtlTicks cannot be negative");
        }
        Objects.requireNonNull(random, "random");
    }

    public OrderSelectionContext toSelectionContext(java.util.List<com.y271727uy.shopcore.core.order.ShopListing> listings) {
        return new OrderSelectionContext(
                shop.shopId(),
                shop.shopPos(),
                shop.shopTier(),
                shop.marketTier(),
                reputation,
                listings,
                customerProfile,
                gameTime,
                orderTtlTicks,
                random
        );
    }
}

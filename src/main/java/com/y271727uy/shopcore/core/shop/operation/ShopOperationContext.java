package com.y271727uy.shopcore.core.shop.operation;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRuleSet;
import net.minecraft.util.RandomSource;

import java.util.Objects;

public record ShopOperationContext(
        ShopInstance shop,
        ShopMenuSnapshot menuSnapshot,
        DemandPoolCatalog demandPoolCatalog,
        ShopOpeningRuleSet openingRuleSet,
        ShopOrderBook orderBook,
        boolean openRequested,
        long dayTime,
        long gameTime,
        RandomSource random
) {
    public ShopOperationContext {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(menuSnapshot, "menuSnapshot");
        Objects.requireNonNull(demandPoolCatalog, "demandPoolCatalog");
        Objects.requireNonNull(openingRuleSet, "openingRuleSet");
        Objects.requireNonNull(orderBook, "orderBook");
        if (!shop.shopId().equals(menuSnapshot.shopId())) {
            throw new IllegalArgumentException("menu snapshot belongs to another shop");
        }
        if (!shop.shopId().equals(orderBook.shopId())) {
            throw new IllegalArgumentException("order book belongs to another shop");
        }
        if (dayTime < 0L) {
            throw new IllegalArgumentException("dayTime cannot be negative");
        }
        if (gameTime < 0L) {
            throw new IllegalArgumentException("gameTime cannot be negative");
        }
        Objects.requireNonNull(random, "random");
    }
}

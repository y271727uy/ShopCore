package com.y271727uy.shopcore.core.order.generation;

import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import net.minecraft.util.RandomSource;

import java.util.Objects;

public record OrderGenerationScheduleContext(
        ShopInstance shop,
        ShopOrderBook orderBook,
        long dayTime,
        long gameTime,
        RandomSource random
) {
    public OrderGenerationScheduleContext {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(orderBook, "orderBook");
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

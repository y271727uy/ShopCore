package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.order.book.ShopOrderBook;

public interface ShopOrderBookHolder {
    ShopOrderBook shopcore$orderBook();

    void shopcore$setOrderBook(ShopOrderBook orderBook);
}

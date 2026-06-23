package com.y271727uy.shopcore.core.order.storage;

import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.storage.ShopIdNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ShopOrderBookNbt {
    public static final String ORDER_BOOK = "OrderBook";

    private static final String SHOP_ID = "ShopId";
    private static final String ORDERS = "Orders";

    private ShopOrderBookNbt() {
    }

    public static CompoundTag save(ShopOrderBook orderBook) {
        Objects.requireNonNull(orderBook, "orderBook");
        CompoundTag tag = new CompoundTag();
        ShopIdNbt.put(tag, SHOP_ID, orderBook.shopId());
        ListTag orders = new ListTag();
        for (ShopOrder order : orderBook.orders()) {
            orders.add(ShopOrderNbt.save(order));
        }
        tag.put(ORDERS, orders);
        return tag;
    }

    public static ShopOrderBook load(CompoundTag tag, ShopId fallbackShopId, BlockPos fallbackShopPos) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fallbackShopId, "fallbackShopId");
        Objects.requireNonNull(fallbackShopPos, "fallbackShopPos");
        ShopId shopId = ShopIdNbt.get(tag, SHOP_ID, fallbackShopId);
        List<ShopOrder> orders = new ArrayList<>();
        ListTag list = tag.getList(ORDERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ShopOrderNbt.load(list.getCompound(i), shopId, fallbackShopPos)
                    .filter(order -> shopId.equals(order.shopId()))
                    .ifPresent(orders::add);
        }
        return new ShopOrderBook(shopId, orders);
    }
}

package com.y271727uy.shopcore.core.shop.storage;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.menu.storage.ShopMenuSnapshotNbt;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.storage.ShopOrderBookNbt;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public final class ShopRuntimeNbt {
    private static final String OPEN_REQUESTED = "OpenRequested";

    private ShopRuntimeNbt() {
    }

    public static CompoundTag save(ShopInstance shop, ShopMenuSnapshot menuSnapshot, ShopOrderBook orderBook, boolean openRequested) {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(menuSnapshot, "menuSnapshot");
        Objects.requireNonNull(orderBook, "orderBook");
        CompoundTag tag = new CompoundTag();
        tag.put(ShopInstanceNbt.SHOP, ShopInstanceNbt.save(shop));
        tag.put(ShopMenuSnapshotNbt.MENU, ShopMenuSnapshotNbt.save(menuSnapshot));
        tag.put(ShopOrderBookNbt.ORDER_BOOK, ShopOrderBookNbt.save(orderBook));
        tag.putBoolean(OPEN_REQUESTED, openRequested);
        return tag;
    }

    public static ShopRuntimeSnapshot load(CompoundTag tag, BlockPos fallbackShopPos) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fallbackShopPos, "fallbackShopPos");
        ShopInstance shop = tag.contains(ShopInstanceNbt.SHOP)
                ? ShopInstanceNbt.load(tag.getCompound(ShopInstanceNbt.SHOP), fallbackShopPos)
                : ShopInstance.create(fallbackShopPos);
        ShopMenuSnapshot menuSnapshot = tag.contains(ShopMenuSnapshotNbt.MENU)
                ? ShopMenuSnapshotNbt.load(tag.getCompound(ShopMenuSnapshotNbt.MENU), shop.shopId())
                : ShopMenuSnapshot.empty(shop.shopId());
        ShopOrderBook orderBook = tag.contains(ShopOrderBookNbt.ORDER_BOOK)
                ? ShopOrderBookNbt.load(tag.getCompound(ShopOrderBookNbt.ORDER_BOOK), shop.shopId(), shop.shopPos())
                : ShopOrderBook.empty(shop.shopId());
        return new ShopRuntimeSnapshot(shop, menuSnapshot, orderBook, tag.getBoolean(OPEN_REQUESTED));
    }
}

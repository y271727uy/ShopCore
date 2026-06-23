package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.storage.ShopRuntimeNbt;
import com.y271727uy.shopcore.core.shop.storage.ShopRuntimeSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Objects;

public final class ShopRuntimePersistence {
    private ShopRuntimePersistence() {
    }

    public static void load(ShopBlockRuntimeHolder holder, CompoundTag tag, BlockPos fallbackShopPos) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fallbackShopPos, "fallbackShopPos");
        ShopRuntimeSnapshot snapshot = ShopRuntimeNbt.load(tag, fallbackShopPos);
        holder.shopcore$setShopInstance(snapshot.shop());
        holder.shopcore$setMenuSnapshot(snapshot.menuSnapshot());
        holder.shopcore$setOrderBook(snapshot.orderBook());
        holder.shopcore$setOpenRequested(snapshot.openRequested());
    }

    public static CompoundTag save(ShopBlockRuntimeHolder holder) {
        Objects.requireNonNull(holder, "holder");
        return ShopRuntimeNbt.save(
                holder.shopcore$shopInstance(),
                holder.shopcore$menuSnapshot(),
                holder.shopcore$orderBook(),
                holder.shopcore$openRequested()
        );
    }

    public static void initializeIfMissing(ShopBlockRuntimeHolder holder, BlockPos shopPos) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(shopPos, "shopPos");
        ShopInstance shop = holder.shopcore$shopInstance();
        if (shop == null) {
            shop = ShopInstance.create(shopPos);
            holder.shopcore$setShopInstance(shop);
        }
        ShopMenuSnapshot menuSnapshot = holder.shopcore$menuSnapshot();
        if (menuSnapshot == null || !shop.shopId().equals(menuSnapshot.shopId())) {
            holder.shopcore$setMenuSnapshot(ShopMenuSnapshot.empty(shop.shopId()));
        }
        ShopOrderBook orderBook = holder.shopcore$orderBook();
        if (orderBook == null || !shop.shopId().equals(orderBook.shopId())) {
            holder.shopcore$setOrderBook(ShopOrderBook.empty(shop.shopId()));
        }
    }
}

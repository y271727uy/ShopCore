package com.y271727uy.shopcore.core.menu;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.Objects;
import java.util.Optional;

public class BlockEntityShopMenuRepository implements ShopMenuRepository {
    private final ShopMenuHolder holder;

    public BlockEntityShopMenuRepository(ShopMenuHolder holder) {
        this.holder = Objects.requireNonNull(holder, "holder");
    }

    @Override
    public Optional<ShopMenuSnapshot> find(ShopId shopId) {
        Objects.requireNonNull(shopId, "shopId");
        ShopMenuSnapshot snapshot = holder.menuSnapshot();
        if (snapshot == null || !snapshot.shopId().equals(shopId)) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    @Override
    public ShopMenuSnapshot save(ShopMenuSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (!holder.shopId().equals(snapshot.shopId())) {
            throw new IllegalArgumentException("snapshot shop id does not match holder shop id");
        }
        holder.setMenuSnapshot(snapshot);
        return snapshot;
    }

    @Override
    public boolean delete(ShopId shopId) {
        Objects.requireNonNull(shopId, "shopId");
        if (!holder.shopId().equals(shopId)) {
            return false;
        }
        holder.setMenuSnapshot(ShopMenuSnapshot.empty(shopId));
        return true;
    }
}

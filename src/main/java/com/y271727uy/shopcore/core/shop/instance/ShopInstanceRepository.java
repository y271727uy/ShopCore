package com.y271727uy.shopcore.core.shop.instance;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Storage boundary for shop instances.
 * Implementations may be backed by block entities, level SavedData, or tests.
 */
public interface ShopInstanceRepository {
    Optional<ShopInstance> find(ShopId shopId);

    Optional<ShopInstance> findByPos(BlockPos shopPos);

    List<ShopInstance> findAll();

    ShopInstance save(ShopInstance shop);

    boolean delete(ShopId shopId);

    default ShopInstance getOrCreate(BlockPos shopPos) {
        Objects.requireNonNull(shopPos, "shopPos");
        return findByPos(shopPos).orElseGet(() -> save(ShopInstance.create(shopPos)));
    }
}

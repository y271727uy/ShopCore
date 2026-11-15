package com.y271727uy.shopcore.core.shop.ownership;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * No ownership assigned - anyone can claim the shop
 */
public record NoOwnership(ShopId shopId) implements Ownership {

    public NoOwnership {
        Objects.requireNonNull(shopId, "shopId");
    }

    @Override
    public OwnershipType type() {
        return OwnershipType.NONE;
    }

    @Override
    public boolean isOwner(UUID playerId) {
        return false;
    }

    @Override
    public boolean canManage(UUID playerId) {
        return false;
    }

    @Override
    public List<UUID> getAllOwners() {
        return Collections.emptyList();
    }

    /**
     * Anyone can claim an unowned shop
     */
    @Override
    public boolean canModify(UUID playerId) {
        return true;
    }

    /**
     * Anyone can operate an unowned shop
     */
    @Override
    public boolean canOperate(UUID playerId) {
        return true;
    }
}

package com.y271727uy.shopcore.core.shop.ownership;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Single player ownership - simplest form
 *
 * One player owns and operates the shop.
 * All revenue goes to this player.
 */
public record SingleOwnership(
    ShopId shopId,
    UUID ownerId
) implements Ownership {

    public SingleOwnership {
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(ownerId, "ownerId");
    }

    public static SingleOwnership create(ShopId shopId, UUID ownerId) {
        return new SingleOwnership(shopId, ownerId);
    }

    @Override
    public OwnershipType type() {
        return OwnershipType.SINGLE;
    }

    @Override
    public boolean isOwner(UUID playerId) {
        return ownerId.equals(playerId);
    }

    @Override
    public boolean canManage(UUID playerId) {
        return ownerId.equals(playerId);
    }

    @Override
    public List<UUID> getAllOwners() {
        return Collections.singletonList(ownerId);
    }
}

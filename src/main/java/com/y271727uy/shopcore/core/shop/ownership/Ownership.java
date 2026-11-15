package com.y271727uy.shopcore.core.shop.ownership;

import com.y271727uy.shopcore.core.shop.instance.ShopId;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

/**
 * Shop ownership abstraction.
 *
 * Defines who can operate a shop and how revenue is distributed.
 * Implementations range from single-owner to team-based ownership.
 */
public interface Ownership {

    /**
     * Shop ID this ownership belongs to
     */
    ShopId shopId();

    /**
     * Ownership type identifier
     */
    OwnershipType type();

    /**
     * Check if a player is an owner (any level of membership)
     *
     * @param playerId player UUID
     * @return true if the player is an owner/member
     */
    boolean isOwner(UUID playerId);

    /**
     * Check if a player can manage the shop (invite, kick, change settings)
     *
     * @param playerId player UUID
     * @return true if the player has management permissions
     */
    boolean canManage(UUID playerId);

    /**
     * Get all owner/member UUIDs
     *
     * @return list of all owner/member UUIDs
     */
    List<UUID> getAllOwners();

    /**
     * Check if this ownership can be modified by the given player
     *
     * @param playerId player UUID
     * @return true if the player can change ownership structure
     */
    default boolean canModify(UUID playerId) {
        return canManage(playerId);
    }

    /**
     * Check if a player can operate the shop (open, close, accept orders)
     *
     * @param playerId player UUID
     * @return true if the player can operate
     */
    default boolean canOperate(UUID playerId) {
        return isOwner(playerId);
    }
}

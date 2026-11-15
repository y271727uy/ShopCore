package com.y271727uy.shopcore.core.shop.ownership;

import java.util.Objects;
import java.util.UUID;

/**
 * Team member data
 */
public record TeamMember(
    UUID playerId,
    TeamRole role,
    long joinedGameTime
) {
    public TeamMember {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(role, "role");
        if (joinedGameTime < 0L) {
            throw new IllegalArgumentException("joinedGameTime cannot be negative");
        }
    }

    public static TeamMember founder(UUID playerId, long gameTime) {
        return new TeamMember(playerId, TeamRole.FOUNDER, gameTime);
    }

    public static TeamMember manager(UUID playerId, long gameTime) {
        return new TeamMember(playerId, TeamRole.MANAGER, gameTime);
    }

    public static TeamMember member(UUID playerId, long gameTime) {
        return new TeamMember(playerId, TeamRole.MEMBER, gameTime);
    }

    public TeamMember withRole(TeamRole newRole) {
        return new TeamMember(playerId, newRole, joinedGameTime);
    }

    public boolean canManage() {
        return role.canManage();
    }

    public boolean isFounder() {
        return role == TeamRole.FOUNDER;
    }
}

package com.y271727uy.shopcore.core.shop.ownership;

import com.y271727uy.shopcore.core.shop.instance.ShopId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Team ownership - multiple players share ownership
 *
 * - Founder creates the team
 * - Managers can invite/kick members
 * - Members can operate and receive revenue shares
 * - Revenue distribution uses Shapley Value or configured distributor
 */
public record TeamOwnership(
    ShopId shopId,
    UUID founderId,
    Map<UUID, TeamMember> members
) implements Ownership {

    public TeamOwnership {
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(founderId, "founderId");
        members = Map.copyOf(Objects.requireNonNull(members, "members"));

        // Founder must be in members
        if (!members.containsKey(founderId)) {
            throw new IllegalArgumentException("Founder must be a team member");
        }

        // Founder must have FOUNDER role
        TeamMember founder = members.get(founderId);
        if (founder.role() != TeamRole.FOUNDER) {
            throw new IllegalArgumentException("Founder must have FOUNDER role");
        }
    }

    /**
     * Create a new team with a single founder
     */
    public static TeamOwnership create(ShopId shopId, UUID founderId, long gameTime) {
        Map<UUID, TeamMember> members = new HashMap<>();
        members.put(founderId, TeamMember.founder(founderId, gameTime));
        return new TeamOwnership(shopId, founderId, members);
    }

    @Override
    public OwnershipType type() {
        return OwnershipType.TEAM;
    }

    @Override
    public boolean isOwner(UUID playerId) {
        return members.containsKey(playerId);
    }

    @Override
    public boolean canManage(UUID playerId) {
        TeamMember member = members.get(playerId);
        return member != null && member.canManage();
    }

    @Override
    public List<UUID> getAllOwners() {
        return new ArrayList<>(members.keySet());
    }

    /**
     * Add a new member to the team
     *
     * @param playerId player to add
     * @param role role to assign
     * @param gameTime current game time
     * @return updated ownership
     */
    public TeamOwnership addMember(UUID playerId, TeamRole role, long gameTime) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(role, "role");

        if (members.containsKey(playerId)) {
            throw new IllegalArgumentException("Player is already a member");
        }

        if (role == TeamRole.FOUNDER) {
            throw new IllegalArgumentException("Cannot add another founder");
        }

        Map<UUID, TeamMember> updated = new HashMap<>(members);
        updated.put(playerId, new TeamMember(playerId, role, gameTime));
        return new TeamOwnership(shopId, founderId, updated);
    }

    /**
     * Remove a member from the team
     *
     * @param playerId player to remove
     * @return updated ownership
     */
    public TeamOwnership removeMember(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");

        if (playerId.equals(founderId)) {
            throw new IllegalArgumentException("Cannot remove founder");
        }

        if (!members.containsKey(playerId)) {
            throw new IllegalArgumentException("Player is not a member");
        }

        Map<UUID, TeamMember> updated = new HashMap<>(members);
        updated.remove(playerId);
        return new TeamOwnership(shopId, founderId, updated);
    }

    /**
     * Change a member's role
     *
     * @param playerId player to modify
     * @param newRole new role
     * @return updated ownership
     */
    public TeamOwnership changeRole(UUID playerId, TeamRole newRole) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(newRole, "newRole");

        TeamMember member = members.get(playerId);
        if (member == null) {
            throw new IllegalArgumentException("Player is not a member");
        }

        if (playerId.equals(founderId) && newRole != TeamRole.FOUNDER) {
            throw new IllegalArgumentException("Cannot change founder's role");
        }

        if (newRole == TeamRole.FOUNDER) {
            throw new IllegalArgumentException("Cannot promote to founder");
        }

        Map<UUID, TeamMember> updated = new HashMap<>(members);
        updated.put(playerId, member.withRole(newRole));
        return new TeamOwnership(shopId, founderId, updated);
    }

    /**
     * Get a specific member
     */
    public Optional<TeamMember> getMember(UUID playerId) {
        return Optional.ofNullable(members.get(playerId));
    }

    /**
     * Get all members with a specific role
     */
    public List<TeamMember> getMembersByRole(TeamRole role) {
        return members.values().stream()
            .filter(m -> m.role() == role)
            .collect(Collectors.toList());
    }

    /**
     * Get team size
     */
    public int size() {
        return members.size();
    }
}

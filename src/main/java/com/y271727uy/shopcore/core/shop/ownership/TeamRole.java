package com.y271727uy.shopcore.core.shop.ownership;

/**
 * Team member role
 */
public enum TeamRole {
    /**
     * Founder - full control, cannot be kicked
     */
    FOUNDER,

    /**
     * Manager - can invite/kick members, manage settings
     */
    MANAGER,

    /**
     * Member - can operate the shop, receives revenue share
     */
    MEMBER;

    /**
     * Check if this role can manage the team
     */
    public boolean canManage() {
        return this == FOUNDER || this == MANAGER;
    }

    /**
     * Check if this role can be modified by another role
     */
    public boolean canBeModifiedBy(TeamRole other) {
        if (this == FOUNDER) {
            return false; // Founder cannot be modified
        }
        if (other == FOUNDER) {
            return true; // Founder can modify anyone
        }
        if (other == MANAGER && this == MEMBER) {
            return true; // Manager can modify members
        }
        return false;
    }
}

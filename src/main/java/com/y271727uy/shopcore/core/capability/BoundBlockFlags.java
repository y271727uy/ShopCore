package com.y271727uy.shopcore.core.capability;

/**
 * Internal duck interface mixed into {@link net.minecraft.world.level.block.Block}, NOT an API.
 * Caches per-block binding state so hot BlockState paths read one field instead of probing maps.
 * Written only by {@link BlockBindingRegistry} during (synchronized) setup registration.
 */
public interface BoundBlockFlags {
    /** The block gets a bound host BlockEntity (explicit binding or capability carrier). */
    byte HOST = 1;
    /** The block has capability bindings (attached to any BlockEntity it ends up with). */
    byte CAPABILITIES = 2;

    byte shopcore$bindingFlags();

    void shopcore$setBindingFlags(byte flags);
}

package com.y271727uy.shopcore.mixin;

import com.y271727uy.shopcore.core.capability.BoundBlockFlags;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
abstract class BoundBlockMixin implements BoundBlockFlags {
    @Unique
    private byte shopcore$bindingFlags;

    @Override
    public byte shopcore$bindingFlags() { return shopcore$bindingFlags; }

    @Override
    public void shopcore$setBindingFlags(byte flags) { shopcore$bindingFlags = flags; }
}

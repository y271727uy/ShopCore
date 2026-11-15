package com.y271727uy.shopcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.y271727uy.shopcore.core.capability.BlockBindingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class BoundBlockStateMixin {
    // Hot paths for every BlockState: ModifyReturnValue allocates no CallbackInfo and keeps other mods'
    // injections in the original body running. Bound hosts are never EntityBlock (enforced at registration),
    // so the vanilla result for them is always false / null and is safe to replace.
    @ModifyReturnValue(method = "hasBlockEntity", at = @At("RETURN"))
    private boolean shopcore$hasBoundEntity(boolean original) {
        return original || BlockBindingHooks.hasBinding((BlockState) (Object) this);
    }

    @ModifyReturnValue(method = "getTicker", at = @At("RETURN"))
    private <T extends BlockEntity> BlockEntityTicker<T> shopcore$boundTicker(
            BlockEntityTicker<T> original, Level level, BlockEntityType<T> type) {
        BlockState state = (BlockState) (Object) this;
        return BlockBindingHooks.hasBinding(state) ? BlockBindingHooks.ticker(state, level, type) : original;
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void shopcore$removeBoundEntity(Level level, BlockPos pos, BlockState replacement,
                                           boolean moving, CallbackInfo ci) {
        BlockState state = (BlockState) (Object) this;
        // Covers ordinary blocks whose onRemove overrides do not call BlockBehaviour.onRemove.
        if (BlockBindingHooks.hasBinding(state) && state.getBlock() != replacement.getBlock()) {
            level.removeBlockEntity(pos);
        }
    }
}

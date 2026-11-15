package com.y271727uy.shopcore.mixin;

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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class BoundBlockStateMixin {
    @Inject(method = "hasBlockEntity", at = @At("HEAD"), cancellable = true)
    private void shopcore$hasBoundEntity(CallbackInfoReturnable<Boolean> cir) {
        if (BlockBindingHooks.hasBinding((BlockState) (Object) this)) cir.setReturnValue(true);
    }

    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void shopcore$boundTicker(Level level, BlockEntityType<T> type,
                                                           CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        BlockState state = (BlockState) (Object) this;
        if (BlockBindingHooks.hasBinding(state)) cir.setReturnValue(BlockBindingHooks.ticker(state, level, type));
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

package com.y271727uy.shopcore.mixin;

import com.y271727uy.shopcore.core.capability.BlockBindingHooks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityType.class)
abstract class BoundBlockEntityTypeMixin {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void shopcore$validateBoundType(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (BlockBindingHooks.hasBinding(state)) {
            cir.setReturnValue(BlockBindingHooks.accepts(state, (BlockEntityType<?>) (Object) this));
        }
    }
}

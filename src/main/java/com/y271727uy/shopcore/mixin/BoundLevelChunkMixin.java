package com.y271727uy.shopcore.mixin;

import com.y271727uy.shopcore.core.capability.BlockBindingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
abstract class BoundLevelChunkMixin {
    @Inject(method = "createBlockEntity", at = @At("HEAD"), cancellable = true)
    private void shopcore$createAtPosition(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        BlockState state = ((LevelChunk) (Object) this).getBlockState(pos);
        if (BlockBindingHooks.hasBinding(state)) cir.setReturnValue(BlockBindingHooks.create(pos, state));
    }

    @Inject(method = "promotePendingBlockEntity", at = @At("HEAD"), cancellable = true)
    private void shopcore$promoteDummy(BlockPos pos, CompoundTag tag, CallbackInfoReturnable<BlockEntity> cir) {
        LevelChunk chunk = (LevelChunk) (Object) this;
        BlockState state = chunk.getBlockState(pos);
        if ("DUMMY".equals(tag.getString("id")) && BlockBindingHooks.hasBinding(state)) {
            BlockEntity entity = BlockBindingHooks.create(pos, state);
            chunk.addAndRegisterBlockEntity(entity);
            cir.setReturnValue(entity);
        }
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;getBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk$EntityCreationType;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity shopcore$createBoundEntity(LevelChunk chunk, BlockPos pos, LevelChunk.EntityCreationType mode) {
        BlockEntity entity = chunk.getBlockEntity(pos, mode);
        BlockState state = chunk.getBlockState(pos);
        if (entity == null && BlockBindingHooks.hasBinding(state)) {
            entity = BlockBindingHooks.create(pos, state);
            chunk.addAndRegisterBlockEntity(entity);
        }
        return entity;
    }
}

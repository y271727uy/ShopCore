package com.y271727uy.shopcore.mixin;

import com.y271727uy.shopcore.core.capability.BlockBindingHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** World generation also casts ordinary blocks to EntityBlock when promoting DUMMY NBT. */
@Mixin(WorldGenRegion.class)
abstract class BoundWorldGenRegionMixin {
    @Shadow
    protected abstract void markPosForPostprocessing(BlockPos pos);

    @Redirect(method = "setBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkAccess;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private void shopcore$invalidateRemovedHost(ChunkAccess chunk, BlockPos pos) {
        BlockEntity entity = chunk.getBlockEntity(pos);
        if (entity != null && BlockBindingHooks.hasBinding(entity.getBlockState())) entity.setRemoved();
        chunk.removeBlockEntity(pos);
    }

    @Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
    private void shopcore$readBoundEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        WorldGenRegion region = (WorldGenRegion) (Object) this;
        ChunkAccess chunk = region.getChunk(pos);
        BlockState state = chunk.getBlockState(pos);
        if (!BlockBindingHooks.hasBinding(state)) return;
        BlockEntity entity = chunk.getBlockEntity(pos);
        if (entity == null) {
            CompoundTag tag = chunk.getBlockEntityNbt(pos);
            if (tag != null) {
                entity = "DUMMY".equals(tag.getString("id"))
                        ? BlockBindingHooks.create(pos, state) : BlockEntity.loadStatic(pos, state, tag);
                if (entity != null) chunk.setBlockEntity(entity);
            }
        }
        cir.setReturnValue(entity);
    }

    @Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
    private void shopcore$placeBoundEntity(BlockPos pos, BlockState state, int flags, int recursionLeft,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!BlockBindingHooks.hasBinding(state)) return;
        WorldGenRegion region = (WorldGenRegion) (Object) this;
        if (!region.ensureCanWrite(pos)) {
            cir.setReturnValue(false);
            return;
        }
        // Mirrors the vanilla bound-block branch, without the EntityBlock cast.
        ChunkAccess chunk = region.getChunk(pos);
        BlockState previous = chunk.setBlockState(pos, state, false);
        if (previous != null) region.getLevel().onBlockStateChange(pos, previous, state);
        if (chunk.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
            // LevelChunk.setBlockState already created/updated the host; don't create it twice.
            if (chunk.getBlockEntity(pos) == null) chunk.setBlockEntity(BlockBindingHooks.create(pos, state));
        } else {
            BlockEntity existing = chunk.getBlockEntity(pos);
            if (existing != null && (previous == null || previous.getBlock() != state.getBlock()
                    || !BlockBindingHooks.accepts(state, existing.getType()))) {
                existing.setRemoved();
                chunk.removeBlockEntity(pos);
                existing = null;
            }
            if (existing != null) {
                existing.setBlockState(state);
            } else {
                CompoundTag dummy = new CompoundTag();
                dummy.putInt("x", pos.getX());
                dummy.putInt("y", pos.getY());
                dummy.putInt("z", pos.getZ());
                dummy.putString("id", "DUMMY");
                chunk.setBlockEntityNbt(dummy);
            }
        }
        if (state.hasPostProcess(region, pos)) markPosForPostprocessing(pos);
        cir.setReturnValue(true);
    }
}

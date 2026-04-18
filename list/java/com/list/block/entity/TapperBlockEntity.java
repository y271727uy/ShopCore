package com.list.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import javax.annotation.Nullable;
import net.minecraft.MethodsReturnNonnullByDefault;
import javax.annotation.ParametersAreNonnullByDefault;
import com.list.block.TapperBlock;
import net.minecraft.server.level.ServerPlayer;
import com.list.block.TapperRecipe;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TapperBlockEntity extends BlockEntity {
    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public final LazyOptional<IItemHandler> handlerCap = LazyOptional.of(() -> itemHandler);

    // persistent fields mirrored from KubeJS
    public long startTime = 0L;
    public int recipeTime = 0;
    public boolean initialized = false;

    public TapperBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putLong("startTime", startTime);
        tag.putInt("recipeTime", recipeTime);
        tag.putBoolean("initialized", initialized);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("startTime")) this.startTime = tag.getLong("startTime");
        if (tag.contains("recipeTime")) this.recipeTime = tag.getInt("recipeTime");
        if (tag.contains("initialized")) this.initialized = tag.getBoolean("initialized");
    }

    @SuppressWarnings("null")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlerCap.invalidate();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        // server-side tick logic ported from KubeJS
        if (level.isClientSide) return;

        boolean working = state.hasProperty(TapperBlock.WORKING) && state.getValue(TapperBlock.WORKING);
        boolean mature = state.hasProperty(TapperBlock.MATURE) && state.getValue(TapperBlock.MATURE);

        TapperRecipe recipe = TapperBlock.getAttachedRecipe(level, pos, state.getValue(TapperBlock.FACING));

        if (!initialized && recipe != null) {
            initialized = true;
            setChanged();
            level.setBlock(pos, state.setValue(TapperBlock.WORKING, true).setValue(TapperBlock.MATURE, false).setValue(TapperBlock.FACING, state.getValue(TapperBlock.FACING)), 3);
            this.startTime = level.getGameTime();
            this.recipeTime = recipe.time;
            setChanged();
            return;
        }

        if (working && !mature && recipe != null) {
            long elapsed = level.getGameTime() - this.startTime;
            int total = this.recipeTime > 0 ? this.recipeTime : recipe.time;
            if (elapsed >= total) {
                level.setBlock(pos, state.setValue(TapperBlock.WORKING, true).setValue(TapperBlock.MATURE, true).setValue(TapperBlock.FACING, state.getValue(TapperBlock.FACING)), 3);

                // Try to send a client sound packet directly to nearest server player; fallback to level.playSound
                try {
                        double px = pos.getX() + 0.5;
                        double py = pos.getY() + 0.5;
                        double pz = pos.getZ() + 0.5;
                        net.minecraft.world.entity.player.Player np = level.getNearestPlayer(px, py, pz, 16.0, false);

                        if (np instanceof ServerPlayer sp) {
                            try {
                                Class<?> pktCls = Class.forName("net.minecraft.network.protocol.game.ClientboundSoundPacket");
                                java.lang.reflect.Constructor<?> chosen = null;
                                for (var c : pktCls.getConstructors()) {
                                    var params = c.getParameterTypes();
                                    if (params.length >= 6 && params[0].getName().equals("net.minecraft.sounds.SoundEvent") && params[1].getName().equals("net.minecraft.sounds.SoundSource")) {
                                        chosen = c;
                                        break;
                                    }
                                }
                                if (chosen != null) {
                                    Object packet = chosen.newInstance(SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, px, py, pz, 1.0f, 1.0f);
                                    sp.connection.send((net.minecraft.network.protocol.Packet<?>) packet);
                                } else {
                                    level.playSound(sp, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                                }
                            } catch (Throwable ex) {
                                level.playSound(sp, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                            }
                        } else {
                            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                } catch (Throwable t) {
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                setChanged();
            }
            return;
        }

        if (working && recipe == null) {
            level.setBlock(pos, state.setValue(TapperBlock.WORKING, false).setValue(TapperBlock.MATURE, false).setValue(TapperBlock.FACING, state.getValue(TapperBlock.FACING)), 3);
            this.startTime = 0L;
            this.recipeTime = 0;
            this.initialized = false;
            setChanged();
        }
    }
}



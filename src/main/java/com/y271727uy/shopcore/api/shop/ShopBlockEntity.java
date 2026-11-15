package com.y271727uy.shopcore.api.shop;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Public block host. Subclasses supply configuration and gameplay feedback through API types only. */
public abstract class ShopBlockEntity extends BlockEntity implements ShopRuntimeHost, ShopFeedback {
    private final ShopRuntime runtime;

    protected ShopBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ShopDefinition definition) {
        super(type, pos, state);
        runtime = ShopcoreShops.createRuntime(this, definition, this::shopFacing, this);
    }

    protected Direction shopFacing() {
        return Direction.NORTH;
    }

    @Override
    public final ShopRuntime shopRuntime() {
        return runtime;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        runtime.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        runtime.save(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.merge(runtime.updateTag());
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        runtime.handleUpdateTag(tag);
    }
}

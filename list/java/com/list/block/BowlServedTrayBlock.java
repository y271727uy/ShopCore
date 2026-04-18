package com.list.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public abstract class BowlServedTrayBlock extends StageFoodBlock {

    private final Supplier<? extends Item> servedItem;
    private final Supplier<? extends Item> leftoverDropItem;

    protected BowlServedTrayBlock(BlockBehaviour.Properties properties,
                                  int maxStage,
                                  Supplier<? extends Item> servedItem,
                                  Supplier<? extends Item> leftoverDropItem,
                                  VoxelShape shape,
                                  boolean useShapeForOcclusion) {
        super(properties, maxStage, servedItem, shape, useShapeForOcclusion);
        this.servedItem = servedItem;
        this.leftoverDropItem = leftoverDropItem;
    }

    @Override
    protected boolean canConsume(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack heldStack) {
        return false;
    }

    protected int getLeftoverStage() {
        return getStageProperty().getPossibleValues().stream().max(Integer::compareTo).orElse(0);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.is(Items.BOWL)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            int stage = state.getValue(getStageProperty());
            if (stage >= getLeftoverStage()) {
                popResource(level, pos, new ItemStack(leftoverDropItem.get()));
                level.removeBlock(pos, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 0.9F);
            } else {
                giveServedItem(level, pos, player, heldStack);
                level.setBlock(pos, state.setValue(getStageProperty(), stage + 1), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void giveServedItem(Level level, BlockPos pos, Player player, ItemStack heldStack) {
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        ItemStack servedStack = new ItemStack(servedItem.get());
        if (!player.addItem(servedStack)) {
            popResource(level, pos, servedStack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
        }
    }
}


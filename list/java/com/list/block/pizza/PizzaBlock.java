package com.list.block.pizza;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class PizzaBlock extends Block {

    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, 3);

    private final Supplier<? extends Item> sliceProvider;

    public PizzaBlock(Supplier<? extends Item> sliceProvider, BlockBehaviour.Properties properties) {
        super(properties);
        this.sliceProvider = sliceProvider;
        this.registerDefaultState(this.stateDefinition.any().setValue(SERVINGS, 3));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SERVINGS);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        InteractionResult result = takeServing(level, pos, state, player, handIn);
        return level.isClientSide && result.consumesAction() ? InteractionResult.SUCCESS : result;
    }

    private InteractionResult takeServing(Level level, BlockPos pos, BlockState state, Player player, InteractionHand handIn) {
        int servings = state.getValue(SERVINGS);
        ItemStack heldStack = player.getItemInHand(handIn);
        Item slice = this.sliceProvider.get();
        if (!heldStack.isEmpty() && heldStack.getItem() != slice) {
            popResource(level, pos, new ItemStack(slice));
        } else if (heldStack.getItem() == slice && heldStack.getCount() < heldStack.getMaxStackSize()) {
            heldStack.setCount(heldStack.getCount() + 1);
        } else {
            player.setItemInHand(handIn, new ItemStack(slice));
        }

        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level.getBlockState(pos).getValue(SERVINGS) == 0) {
            level.destroyBlock(pos, false);
        } else if (level.getBlockState(pos).getValue(SERVINGS) > 0) {
            level.setBlock(pos, state.setValue(SERVINGS, servings - 1), 3);
        }

        return InteractionResult.SUCCESS;
    }
}



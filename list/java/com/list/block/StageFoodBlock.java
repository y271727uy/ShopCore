package com.list.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/*
  通用多阶段可食用食物方块。
  stage 递增表示被取食得越来越少；到最后一个阶段后移除方块。
 */
public abstract class StageFoodBlock extends Block {

    private final int maxStage;
    private final Supplier<? extends Item> foodItem;
    private final VoxelShape shape;
    private final boolean useShapeForOcclusion;

    public StageFoodBlock(BlockBehaviour.Properties properties,
                          int maxStage,
                          Supplier<? extends Item> foodItem,
                          VoxelShape shape,
                          boolean useShapeForOcclusion) {
        super(properties);
        this.maxStage = maxStage;
        this.foodItem = foodItem;
        this.shape = shape;
        this.useShapeForOcclusion = useShapeForOcclusion;
        this.registerDefaultState(this.stateDefinition.any().setValue(getStageProperty(), 0));
    }

    public abstract IntegerProperty getStageProperty();

    protected boolean canConsume(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack heldStack) {
        return true;
    }

    protected void onConsume(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack heldStack) {
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(getStageProperty());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!canConsume(state, level, pos, player, hand, heldStack)) {
            return InteractionResult.PASS;
        }

        FoodProperties food = foodItem.get().getFoodProperties();
        if (food == null || !player.canEat(food.canAlwaysEat())) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            player.getFoodData().eat(food.getNutrition(), food.getSaturationModifier());
            onConsume(state, level, pos, player, hand, heldStack);
            int nextStage = state.getValue(getStageProperty()) + 1;
            if (nextStage > maxStage) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(getStageProperty(), nextStage), 3);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return useShapeForOcclusion ? shape : super.getOcclusionShape(state, level, pos);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return useShapeForOcclusion;
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.WOOL;
    }
}






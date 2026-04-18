package com.list.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 通用分阶段 cutout 植物/装饰方块。
 */
public class StagePlantBlock extends BushBlock {

    public static final IntegerProperty STAGE = BlockStateProperties.AGE_7;

    private final int maxStage;
    private final VoxelShape shape;

    public StagePlantBlock(BlockBehaviour.Properties properties, int maxStage, VoxelShape shape) {
        super(properties);
        this.maxStage = maxStage;
        this.shape = shape;
        this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, 0));
    }

    public int getMaxStage() {
        return maxStage;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(STAGE);
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
        return shape;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.GRASS;
    }
}


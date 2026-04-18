package com.list.block.mushroom;

import com.list.block.colony.SeaMushroomColonyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

public class SeaMushroomBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final Properties properties;

    public SeaMushroomBlock(Properties properties) {
        super(properties);
        this.properties = properties;
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        FluidState fluidState = level.getFluidState(pos);
        
        // Must be in water and on appropriate blocks (dirt, grass, sand, gravel, organic compost, rich soil)
        return fluidState.getType() == Fluids.WATER && 
               (belowState.is(Blocks.DIRT) || 
                belowState.is(Blocks.GRASS_BLOCK) || 
                belowState.is(Blocks.SAND) || 
                belowState.is(Blocks.GRAVEL) ||
                isFarmersDelightBlock(belowState, "organic_compost") ||
                isFarmersDelightBlock(belowState, "rich_soil"));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.DIRT) || 
               state.is(Blocks.GRASS_BLOCK) || 
               state.is(Blocks.SAND) || 
               state.is(Blocks.GRAVEL) ||
               isFarmersDelightBlock(state, "organic_compost") ||
               isFarmersDelightBlock(state, "rich_soil");
    }
    
    private boolean isFarmersDelightBlock(BlockState state, String blockName) {
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        return blockId != null && "farmersdelight".equals(blockId.getNamespace()) && blockName.equals(blockId.getPath());
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
    
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }
    
    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true; // Always allow bone meal to work
    }
    
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }
    
    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // 生长为菌落
        if (level.getBlockState(pos).is(this)) {
            level.setBlock(pos, new SeaMushroomColonyBlock(this.properties).defaultBlockState()
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED)), 3);
        }
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
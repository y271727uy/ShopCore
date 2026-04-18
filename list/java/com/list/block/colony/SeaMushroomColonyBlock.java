package com.list.block.colony;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

public class SeaMushroomColonyBlock extends BushBlock implements BonemealableBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 11.0D, 14.0D);

    public SeaMushroomColonyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        FluidState fluidState = level.getFluidState(pos);

        // Must be in water and on appropriate blocks (dirt, grass, sand, gravel, organic compost, rich soil)
        return fluidState.getType() == Fluids.WATER && 
               (belowState.is(Block.byItem(Items.DIRT)) || 
                belowState.is(Block.byItem(Items.GRASS_BLOCK)) || 
                belowState.is(Block.byItem(Items.SAND)) || 
                belowState.is(Block.byItem(Items.GRAVEL)) ||
                isFarmersDelightBlock(belowState, "organic_compost") ||
                isFarmersDelightBlock(belowState, "rich_soil"));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Block.byItem(Items.DIRT)) || 
               state.is(Block.byItem(Items.GRASS_BLOCK)) || 
               state.is(Block.byItem(Items.SAND)) || 
               state.is(Block.byItem(Items.GRAVEL)) ||
               isFarmersDelightBlock(state, "organic_compost") ||
               isFarmersDelightBlock(state, "rich_soil");
    }
    
    private boolean isFarmersDelightBlock(BlockState state, String blockName) {
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        return blockId != null && "farmersdelight".equals(blockId.getNamespace()) && blockName.equals(blockId.getPath());
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        // 骨粉催熟效果
        popResource(level, pos, new ItemStack(Items.RED_MUSHROOM, 1 + random.nextInt(3)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        // 剪刀收获获得海蘑菇
        return new ItemStack(Items.RED_MUSHROOM);
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, HitResult hit) {
        // 玩家空手右键收获
        if (!level.isClientSide) {
            // 播放声音
            level.playSound(null, pos, SoundEvents.MOSS_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // 掉落多个海蘑菇
            popResource(level, pos, new ItemStack(Items.RED_MUSHROOM, 1 + level.random.nextInt(3)));
            
            // 将菌落变回普通大小（模拟部分被采摘）
            // 这里可以根据需要调整实现方式
        }
        
        return InteractionResult.SUCCESS;
    }
    
    // 使方块含水且不会被水流冲走
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
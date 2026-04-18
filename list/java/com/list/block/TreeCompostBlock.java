package com.list.block;

import com.list.all.ModBlockEntities;
import com.list.block.entity.TreeCompostBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class TreeCompostBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    // 硬编码允许种植在树坑上的植物列表
    private static final Set<String> ALLOWED_SAPLINGS = new HashSet<>();
    
    static {
        // 原版树苗
        ALLOWED_SAPLINGS.add("minecraft:oak_sapling");
        ALLOWED_SAPLINGS.add("minecraft:spruce_sapling");
        ALLOWED_SAPLINGS.add("minecraft:birch_sapling");
        ALLOWED_SAPLINGS.add("minecraft:jungle_sapling");
        ALLOWED_SAPLINGS.add("minecraft:acacia_sapling");
        ALLOWED_SAPLINGS.add("minecraft:dark_oak_sapling");
        ALLOWED_SAPLINGS.add("minecraft:mangrove_propagule");
        ALLOWED_SAPLINGS.add("minecraft:cherry_sapling");
        ALLOWED_SAPLINGS.add("extravagantdelight:mango_seedling");
        ALLOWED_SAPLINGS.add("manors_bounty:pomegranate_sapling");
        
        // 可以在这里添加其他mod的树苗，例如:
        // ALLOWED_SAPLINGS.add("someothermod:special_sapling");
    }

    public TreeCompostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TreeCompostBlockEntity(ModBlockEntities.TREE_COMPOST.get(), pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(BlockState state, net.minecraft.world.level.Level level, BlockEntityType<T> type) {
        return null;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
    
    @Override
    public SoundType getSoundType(BlockState state) {
        return SoundType.GRAVEL;
    }
    
    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable) {
        // 获取植物方块的注册名
        Block plantBlock = plantable.getPlant(world, pos.relative(facing)).getBlock();
        ResourceLocation plantId = ForgeRegistries.BLOCKS.getKey(plantBlock);
        String plantIdStr = plantId != null ? plantId.toString() : "";
        
        // 首先检查是否在明确允许的树苗列表中
        if (ALLOWED_SAPLINGS.contains(plantIdStr)) {
            return true;
        }
        
        // 检查植物类型（后备方案）
        PlantType type = plantable.getPlantType(world, pos.relative(facing));
        if (type != null) {
            // 支持植物类型为"tree"的植物（原版树苗等）
            if (type.getName().equals("tree")) {
                return true;
            }
            // 支持PLAINS类型的植物，但排除特定的非树苗植物
            if (type == PlantType.PLAINS) {
                // 排除甜浆果丛等非树苗植物
                return !plantIdStr.contains("sweet_berry_bush");
            }
        }
        
        // 默认不支持种植
        return false;
    }
}
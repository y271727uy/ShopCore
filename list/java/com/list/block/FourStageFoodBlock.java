package com.list.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * 4 个显示阶段（0~3）的可食用方块。
 */
public class FourStageFoodBlock extends StageFoodBlock {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);

    public FourStageFoodBlock(BlockBehaviour.Properties properties,
                              Supplier<? extends Item> foodItem,
                              VoxelShape shape,
                              boolean useShapeForOcclusion) {
        super(properties, 3, foodItem, shape, useShapeForOcclusion);
    }

    @Override
    public IntegerProperty getStageProperty() {
        return STAGE;
    }
}



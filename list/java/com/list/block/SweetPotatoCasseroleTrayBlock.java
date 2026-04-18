package com.list.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class SweetPotatoCasseroleTrayBlock extends BowlServedTrayBlock {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);

    public SweetPotatoCasseroleTrayBlock(BlockBehaviour.Properties properties,
                                         Supplier<? extends Item> servedItem,
                                         Supplier<? extends Item> leftoverDropItem,
                                         VoxelShape shape,
                                         boolean useShapeForOcclusion) {
        super(properties, 3, servedItem, leftoverDropItem, shape, useShapeForOcclusion);
    }

    @Override
    public IntegerProperty getStageProperty() {
        return STAGE;
    }
}


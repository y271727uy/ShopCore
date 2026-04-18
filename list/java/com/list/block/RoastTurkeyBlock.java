package com.list.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class RoastTurkeyBlock extends BowlServedTrayBlock {

    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 5);

    public RoastTurkeyBlock(BlockBehaviour.Properties properties,
                            Supplier<? extends Item> servedItem,
                            VoxelShape shape,
                            boolean useShapeForOcclusion) {
        super(properties, 5, servedItem, () -> Items.BONE_MEAL, shape, useShapeForOcclusion);
    }

    @Override
    public IntegerProperty getStageProperty() {
        return STAGE;
    }
}


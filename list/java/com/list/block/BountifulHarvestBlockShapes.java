package com.list.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bountiful Harvest 系列方块共用形状定义。
 */
public final class BountifulHarvestBlockShapes {

    public static final VoxelShape LOW_TRAY_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
    public static final VoxelShape PIE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 4.0D, 15.0D);

    private BountifulHarvestBlockShapes() {
    }
}

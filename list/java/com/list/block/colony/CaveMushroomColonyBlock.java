package com.list.block.colony;

import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.Random;

public class CaveMushroomColonyBlock extends MushroomBlock {

    public CaveMushroomColonyBlock(Properties properties) {
        super(properties, null);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
    
    // 移除了 @Override 注解，因为这个方法可能不是从父类继承的可重写方法
    public boolean growMushroom(ServerLevel level, BlockPos pos, BlockState state, Random random) {
        // 禁用自然生长
        return false;
    }
}
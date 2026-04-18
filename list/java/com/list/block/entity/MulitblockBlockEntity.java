package com.list.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class MulitblockBlockEntity extends BlockEntity {

    private final Map<String,List<Definition>> patterns = new HashMap<>();

    public MulitblockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public abstract @Nullable List<Definition> createPattern(String patternName);

    public boolean multiblockFormed(String patternName) {
        List<Definition> pattern = patterns.putIfAbsent(patternName, createPattern(patternName));
        if (pattern == null) {
            return false;
        }
        Direction facing = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        BlockPos corePos = getBlockPos();
        for (Definition definition : pattern) {
            if (!definition.check(level, corePos, facing)) {
                return false;
            }
        }
        return true;
    }

    public record Definition(BlockPos dPox, Predicate<BlockState> predicate) {
        public static Definition of(BlockPos dPox, TagKey<Block> tag) {
            return new Definition(dPox, state -> state.is(tag));
        }

        public static Definition of(BlockPos dPox, Block block) {
            return new Definition(dPox, state -> state.is(block));
        }

        public boolean check(Level level, BlockPos corePos, Direction coreDirection) {
            // Guard against nulls which can occur during certain lifecycle calls.
            if (level == null) return false;
            if (corePos == null) return false;

            // Default to SOUTH if direction is not provided
            Direction dir = coreDirection == null ? Direction.SOUTH : coreDirection;

            BlockPos targetPos = getTargetPos(corePos, dir);

            BlockState state = level.getBlockState(targetPos);
            return predicate.test(state);
        }

        private BlockPos getTargetPos(BlockPos corePos, Direction coreDirection) {
            int dx = dPox.getX();
            int dy = dPox.getY();
            int dz = dPox.getZ();

            BlockPos targetPos;
            switch (coreDirection) {
                case NORTH -> targetPos = corePos.offset(-dx, dy, -dz);
                case WEST  -> targetPos = corePos.offset(-dz, dy, dx);
                case EAST  -> targetPos = corePos.offset(dz, dy, -dx);
                default    -> targetPos = corePos.offset(dx, dy, dz);
            }
            return targetPos;
        }
    }
}

package com.y271727uy.shopcore.core.consumer.common.layout;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record ConsumerLayoutTransform(
        BlockPos origin,
        Direction facing
) {
    public ConsumerLayoutTransform {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(facing, "facing");
        if (facing.getAxis().isVertical()) {
            throw new IllegalArgumentException("facing must be horizontal");
        }
    }

    public BlockPos toWorldBlock(BlockPos localOffset) {
        Objects.requireNonNull(localOffset, "localOffset");
        int x = localOffset.getX();
        int y = localOffset.getY();
        int z = localOffset.getZ();
        return switch (facing) {
            case NORTH -> origin.offset(x, y, z);
            case SOUTH -> origin.offset(-x, y, -z);
            case EAST -> origin.offset(-z, y, x);
            case WEST -> origin.offset(z, y, -x);
            default -> throw new IllegalStateException("Unsupported horizontal facing: " + facing);
        };
    }

    public Vec3 toWorldCenter(BlockPos localOffset) {
        return Vec3.atCenterOf(toWorldBlock(localOffset));
    }

    public Vec3 toWorldGroundCenter(BlockPos localOffset) {
        BlockPos blockPos = toWorldBlock(localOffset);
        return new Vec3(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D);
    }

    public Vec3 toWorldPosition(Vec3 localOffset) {
        Objects.requireNonNull(localOffset, "localOffset");
        return switch (facing) {
            case NORTH -> new Vec3(origin.getX() + localOffset.x, origin.getY() + localOffset.y, origin.getZ() + localOffset.z);
            case SOUTH -> new Vec3(origin.getX() - localOffset.x, origin.getY() + localOffset.y, origin.getZ() - localOffset.z);
            case EAST -> new Vec3(origin.getX() - localOffset.z, origin.getY() + localOffset.y, origin.getZ() + localOffset.x);
            case WEST -> new Vec3(origin.getX() + localOffset.z, origin.getY() + localOffset.y, origin.getZ() - localOffset.x);
            default -> throw new IllegalStateException("Unsupported horizontal facing: " + facing);
        };
    }

    public Vec3 toLocalPosition(Vec3 worldPosition) {
        Objects.requireNonNull(worldPosition, "worldPosition");
        double x = worldPosition.x - origin.getX();
        double y = worldPosition.y - origin.getY();
        double z = worldPosition.z - origin.getZ();
        return switch (facing) {
            case NORTH -> new Vec3(x, y, z);
            case SOUTH -> new Vec3(-x, y, -z);
            case EAST -> new Vec3(z, y, -x);
            case WEST -> new Vec3(-z, y, x);
            default -> throw new IllegalStateException("Unsupported horizontal facing: " + facing);
        };
    }
}

package com.y271727uy.shopcore.core.algorithm.astar;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

public record AStarPathResult(
        boolean success,
        List<BlockPos> path,
        AStarPathFailureReason failureReason,
        int visitedNodes
) {
    public AStarPathResult {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(failureReason, "failureReason");
        path = List.copyOf(path);
        if (success && failureReason != AStarPathFailureReason.NONE) {
            throw new IllegalArgumentException("successful path result must use NONE failure reason");
        }
        if (!success && failureReason == AStarPathFailureReason.NONE) {
            throw new IllegalArgumentException("failed path result must include a failure reason");
        }
        if (visitedNodes < 0) {
            throw new IllegalArgumentException("visitedNodes must be non-negative");
        }
    }

    public static AStarPathResult success(List<BlockPos> path, int visitedNodes) {
        return new AStarPathResult(true, path, AStarPathFailureReason.NONE, visitedNodes);
    }

    public static AStarPathResult failure(AStarPathFailureReason failureReason, int visitedNodes) {
        return new AStarPathResult(false, List.of(), failureReason, visitedNodes);
    }
}

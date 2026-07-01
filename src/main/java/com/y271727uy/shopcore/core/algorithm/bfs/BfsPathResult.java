package com.y271727uy.shopcore.core.algorithm.bfs;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Objects;

public record BfsPathResult(
        boolean success,
        List<BlockPos> path,
        BfsPathFailureReason failureReason,
        int visitedNodes
) {
    public BfsPathResult {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(failureReason, "failureReason");
        path = List.copyOf(path);
        if (success && failureReason != BfsPathFailureReason.NONE) {
            throw new IllegalArgumentException("successful path result must use NONE failure reason");
        }
        if (!success && failureReason == BfsPathFailureReason.NONE) {
            throw new IllegalArgumentException("failed path result must include a failure reason");
        }
        if (visitedNodes < 0) {
            throw new IllegalArgumentException("visitedNodes must be non-negative");
        }
    }

    public static BfsPathResult success(List<BlockPos> path, int visitedNodes) {
        return new BfsPathResult(true, path, BfsPathFailureReason.NONE, visitedNodes);
    }

    public static BfsPathResult failure(BfsPathFailureReason failureReason, int visitedNodes) {
        return new BfsPathResult(false, List.of(), failureReason, visitedNodes);
    }
}

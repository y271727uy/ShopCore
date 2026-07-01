package com.y271727uy.shopcore.core.algorithm.bfs;

import net.minecraft.core.BlockPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

public final class BfsPathfinder {
    public static final int DEFAULT_MAX_NODES = 1024;

    private static final BlockPos[] HORIZONTAL_DIRECTIONS = {
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1)
    };

    private BfsPathfinder() {
    }

    public static BfsPathResult findPath(BlockPos start, BlockPos target, Predicate<BlockPos> walkable) {
        return findPath(start, target, walkable, DEFAULT_MAX_NODES);
    }

    public static BfsPathResult findPath(
            BlockPos start,
            BlockPos target,
            Predicate<BlockPos> walkable,
            int maxNodes
    ) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(walkable, "walkable");
        if (maxNodes <= 0) {
            throw new IllegalArgumentException("maxNodes must be positive");
        }

        if (!walkable.test(start)) {
            return BfsPathResult.failure(BfsPathFailureReason.START_BLOCKED, 0);
        }
        if (!walkable.test(target)) {
            return BfsPathResult.failure(BfsPathFailureReason.TARGET_BLOCKED, 0);
        }
        if (start.equals(target)) {
            return BfsPathResult.success(List.of(start), 1);
        }

        Queue<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();

        frontier.add(start);
        visited.add(start);
        if (visited.size() >= maxNodes) {
            return BfsPathResult.failure(BfsPathFailureReason.SEARCH_LIMIT_REACHED, visited.size());
        }

        while (!frontier.isEmpty()) {
            BlockPos current = frontier.remove();

            for (BlockPos direction : HORIZONTAL_DIRECTIONS) {
                BlockPos next = current.offset(direction);
                if (visited.contains(next)) {
                    continue;
                }
                if (!walkable.test(next)) {
                    continue;
                }
                if (visited.size() >= maxNodes) {
                    return BfsPathResult.failure(BfsPathFailureReason.SEARCH_LIMIT_REACHED, visited.size());
                }

                visited.add(next);
                cameFrom.put(next, current);

                if (next.equals(target)) {
                    return BfsPathResult.success(reconstructPath(start, target, cameFrom), visited.size());
                }
                frontier.add(next);
            }
        }

        return BfsPathResult.failure(BfsPathFailureReason.NO_PATH, visited.size());
    }

    private static List<BlockPos> reconstructPath(BlockPos start, BlockPos target, Map<BlockPos, BlockPos> cameFrom) {
        List<BlockPos> reversedPath = new ArrayList<>();
        BlockPos current = target;
        reversedPath.add(current);

        while (!current.equals(start)) {
            current = cameFrom.get(current);
            if (current == null) {
                throw new IllegalStateException("path chain is broken");
            }
            reversedPath.add(current);
        }

        List<BlockPos> path = new ArrayList<>(reversedPath.size());
        for (int i = reversedPath.size() - 1; i >= 0; i--) {
            path.add(reversedPath.get(i));
        }
        return path;
    }
}

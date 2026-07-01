package com.y271727uy.shopcore.core.algorithm.astar;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;

public final class AStarPathfinder {
    public static final int DEFAULT_MAX_NODES = 1024;

    private static final BlockPos[] HORIZONTAL_DIRECTIONS = {
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1)
    };

    private AStarPathfinder() {
    }

    public static AStarPathResult findPath(BlockPos start, BlockPos target, Predicate<BlockPos> walkable) {
        return findPath(start, target, walkable, DEFAULT_MAX_NODES);
    }

    public static AStarPathResult findPath(
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
            return AStarPathResult.failure(AStarPathFailureReason.START_BLOCKED, 0);
        }
        if (!walkable.test(target)) {
            return AStarPathResult.failure(AStarPathFailureReason.TARGET_BLOCKED, 0);
        }
        if (start.equals(target)) {
            return AStarPathResult.success(List.of(start), 1);
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<BlockPos> closedSet = new HashSet<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Integer> costFromStart = new HashMap<>();

        costFromStart.put(start, 0);
        openSet.add(new Node(start, 0, heuristic(start, target)));

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.remove();
            BlockPos current = currentNode.pos();
            if (!closedSet.add(current)) {
                continue;
            }
            if (closedSet.size() >= maxNodes) {
                return AStarPathResult.failure(AStarPathFailureReason.SEARCH_LIMIT_REACHED, closedSet.size());
            }

            for (BlockPos direction : HORIZONTAL_DIRECTIONS) {
                BlockPos next = current.offset(direction);
                if (closedSet.contains(next)) {
                    continue;
                }
                if (!walkable.test(next)) {
                    continue;
                }

                int nextCost = costFromStart.get(current) + 1;
                Integer knownCost = costFromStart.get(next);
                if (knownCost != null && nextCost >= knownCost) {
                    continue;
                }

                cameFrom.put(next, current);
                costFromStart.put(next, nextCost);
                if (next.equals(target)) {
                    return AStarPathResult.success(reconstructPath(start, target, cameFrom), closedSet.size() + 1);
                }

                openSet.add(new Node(next, nextCost, nextCost + heuristic(next, target)));
            }
        }

        return AStarPathResult.failure(AStarPathFailureReason.NO_PATH, closedSet.size());
    }

    private static int heuristic(BlockPos from, BlockPos target) {
        return Math.abs(from.getX() - target.getX()) + Math.abs(from.getZ() - target.getZ());
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

    private record Node(BlockPos pos, int costFromStart, int estimatedTotalCost) implements Comparable<Node> {
        @Override
        public int compareTo(Node other) {
            int byEstimatedCost = Integer.compare(estimatedTotalCost, other.estimatedTotalCost);
            if (byEstimatedCost != 0) {
                return byEstimatedCost;
            }
            return Integer.compare(costFromStart, other.costFromStart);
        }
    }
}

package com.y271727uy.shopcore.core.algorithm.astar;

public enum AStarPathFailureReason {
    NONE,
    START_BLOCKED,
    TARGET_BLOCKED,
    SEARCH_LIMIT_REACHED,
    NO_PATH
}

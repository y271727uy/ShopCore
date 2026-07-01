package com.y271727uy.shopcore.core.algorithm.bfs;

public enum BfsPathFailureReason {
    NONE,
    START_BLOCKED,
    TARGET_BLOCKED,
    SEARCH_LIMIT_REACHED,
    NO_PATH
}

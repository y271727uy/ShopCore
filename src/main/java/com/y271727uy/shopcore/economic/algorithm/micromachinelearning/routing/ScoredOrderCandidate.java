package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.routing;

import com.y271727uy.shopcore.core.order.OrderCandidate;

public record ScoredOrderCandidate(
        OrderCandidate candidate,
        double score
) {
}

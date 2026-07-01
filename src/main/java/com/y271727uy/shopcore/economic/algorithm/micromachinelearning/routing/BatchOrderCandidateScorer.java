package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.routing;

import com.y271727uy.shopcore.core.order.OrderCandidate;
import com.y271727uy.shopcore.core.order.OrderCandidateScorer;
import com.y271727uy.shopcore.core.order.OrderSelectionContext;

import java.util.List;

public interface BatchOrderCandidateScorer extends OrderCandidateScorer {
    List<ScoredOrderCandidate> scoreCandidates(OrderSelectionContext context, List<OrderCandidate> candidates);
}

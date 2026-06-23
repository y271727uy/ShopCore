package com.y271727uy.shopcore.core.order;

public interface OrderCandidateScorer {
    double score(OrderSelectionContext context, OrderCandidate candidate);
}

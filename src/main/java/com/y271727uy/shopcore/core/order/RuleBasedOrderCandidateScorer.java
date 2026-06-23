package com.y271727uy.shopcore.core.order;

/**
 * Transparent baseline scoring: preferences and affordability decide most of the result.
 */
public final class RuleBasedOrderCandidateScorer implements OrderCandidateScorer {
    public static final RuleBasedOrderCandidateScorer INSTANCE = new RuleBasedOrderCandidateScorer();

    private RuleBasedOrderCandidateScorer() {
    }

    @Override
    public double score(OrderSelectionContext context, OrderCandidate candidate) {
        CustomerProfile profile = context.customerProfile();
        long totalValue = candidate.totalValue();
        if (totalValue > profile.maxBudget() || totalValue < profile.minBudget()) {
            return 0.0D;
        }

        double score = 1.0D;
        if (profile.prefersCategory(candidate.demandCategory())) {
            score *= 1.75D;
        }
        if (profile.prefersComplexity(candidate.complexity())) {
            score *= 1.35D;
        }

        double budgetRatio = profile.maxBudget() == 0 ? 1.0D : Math.min(1.0D, totalValue / (double) profile.maxBudget());
        double affordability = Math.max(0.10D, 1.0D - budgetRatio * profile.priceSensitivity());
        score *= affordability;

        if (candidate.listing().hasKnownStock()) {
            double stockRatio = Math.min(1.0D, candidate.listing().stockCount() / (double) Math.max(1, candidate.quantity()));
            score *= Math.max(0.25D, stockRatio);
        }

        return Math.max(0.0D, score);
    }
}

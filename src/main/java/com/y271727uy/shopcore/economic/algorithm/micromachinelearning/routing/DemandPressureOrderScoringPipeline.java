package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.routing;

import com.y271727uy.shopcore.core.order.OrderCandidate;
import com.y271727uy.shopcore.core.order.OrderCandidateScorer;
import com.y271727uy.shopcore.core.order.OrderSelectionContext;
import com.y271727uy.shopcore.core.order.RuleBasedOrderCandidateScorer;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper.MicroPriceAdjustmentHelper;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper.PriceAdjustmentResult;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.weight.WeightSnapshot;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shared-rule scorer with a small activation budget for shop demand pressure.
 */
public final class DemandPressureOrderScoringPipeline implements BatchOrderCandidateScorer {
    public static final int DEFAULT_ACTIVATION_BUDGET = 32;

    private final OrderCandidateScorer baseScorer;
    private final MicroPriceAdjustmentHelper<ShopId, ResourceLocation> helper;
    private final int activationBudget;

    public DemandPressureOrderScoringPipeline(MicroPriceAdjustmentHelper<ShopId, ResourceLocation> helper) {
        this(RuleBasedOrderCandidateScorer.INSTANCE, helper, DEFAULT_ACTIVATION_BUDGET);
    }

    public DemandPressureOrderScoringPipeline(
            OrderCandidateScorer baseScorer,
            MicroPriceAdjustmentHelper<ShopId, ResourceLocation> helper,
            int activationBudget
    ) {
        this.baseScorer = Objects.requireNonNull(baseScorer, "baseScorer");
        this.helper = Objects.requireNonNull(helper, "helper");
        if (activationBudget < 0) {
            throw new IllegalArgumentException("activationBudget cannot be negative");
        }
        this.activationBudget = activationBudget;
    }

    @Override
    public double score(OrderSelectionContext context, OrderCandidate candidate) {
        double baseScore = baseScorer.score(context, candidate);
        if (baseScore <= 0.0D) {
            return 0.0D;
        }
        WeightSnapshot snapshot = helper.weightOf(context.shopId(), candidate.itemId(), context.gameTime());
        return applyDemandPressure(context, candidate, baseScore, snapshot);
    }

    @Override
    public List<ScoredOrderCandidate> scoreCandidates(OrderSelectionContext context, List<OrderCandidate> candidates) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(candidates, "candidates");

        List<ScoredOrderCandidate> scored = new ArrayList<>(candidates.size());
        for (OrderCandidate candidate : candidates) {
            double baseScore = baseScorer.score(context, candidate);
            if (baseScore > 0.0D) {
                scored.add(new ScoredOrderCandidate(candidate, withJitter(context, baseScore)));
            }
        }
        if (scored.isEmpty() || activationBudget == 0) {
            return List.copyOf(scored);
        }

        Map<ResourceLocation, WeightSnapshot> pressureByItem = helper.weightsOf(context.shopId(), context.gameTime());
        if (pressureByItem.isEmpty()) {
            return List.copyOf(scored);
        }

        List<ScoredOrderCandidate> ranked = scored.stream()
                .sorted(Comparator.comparingDouble(ScoredOrderCandidate::score).reversed())
                .toList();
        int activated = 0;
        List<ScoredOrderCandidate> adjusted = new ArrayList<>(ranked.size());
        for (ScoredOrderCandidate scoredCandidate : ranked) {
            OrderCandidate candidate = scoredCandidate.candidate();
            WeightSnapshot snapshot = pressureByItem.get(candidate.itemId());
            if (activated < activationBudget && snapshot != null && snapshot.weight() > 0.0D) {
                adjusted.add(new ScoredOrderCandidate(
                        candidate,
                        applyDemandPressure(context, candidate, scoredCandidate.score(), snapshot)
                ));
                activated++;
            } else {
                adjusted.add(scoredCandidate);
            }
        }
        return adjusted;
    }

    private double applyDemandPressure(
            OrderSelectionContext context,
            OrderCandidate candidate,
            double baseScore,
            WeightSnapshot snapshot
    ) {
        if (snapshot.weight() <= 0.0D) {
            return baseScore;
        }

        PriceAdjustmentResult pressure = helper.previewSellPrice(
                context.shopId(),
                candidate.itemId(),
                candidate.unitPrice(),
                context.gameTime()
        );
        double pressureFactor = 0.25D + 0.75D * pressure.multiplier();
        return baseScore * pressureFactor;
    }

    private double withJitter(OrderSelectionContext context, double score) {
        return score * (0.85D + context.random().nextDouble() * 0.30D);
    }
}

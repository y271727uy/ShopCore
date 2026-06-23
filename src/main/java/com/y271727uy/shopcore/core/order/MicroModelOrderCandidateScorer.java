package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper.MicroPriceAdjustmentHelper;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper.PriceAdjustmentResult;

import java.util.Objects;

/**
 * Uses the existing micro model as a non-recording score modifier.
 * High shop-item sale pressure lowers the chance that customers pick that item again.
 */
public final class MicroModelOrderCandidateScorer implements OrderCandidateScorer {
    private final OrderCandidateScorer delegate;
    private final MicroPriceAdjustmentHelper<ShopId, net.minecraft.resources.ResourceLocation> helper;

    public MicroModelOrderCandidateScorer(MicroPriceAdjustmentHelper<ShopId, net.minecraft.resources.ResourceLocation> helper) {
        this(RuleBasedOrderCandidateScorer.INSTANCE, helper);
    }

    public MicroModelOrderCandidateScorer(
            OrderCandidateScorer delegate,
            MicroPriceAdjustmentHelper<ShopId, net.minecraft.resources.ResourceLocation> helper
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.helper = Objects.requireNonNull(helper, "helper");
    }

    @Override
    public double score(OrderSelectionContext context, OrderCandidate candidate) {
        double baseScore = delegate.score(context, candidate);
        if (baseScore <= 0.0D) {
            return 0.0D;
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
}

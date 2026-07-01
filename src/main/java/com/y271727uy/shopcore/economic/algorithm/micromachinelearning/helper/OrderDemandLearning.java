package com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper;

import com.y271727uy.shopcore.core.order.OrderCandidateScorer;
import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.routing.DemandPressureOrderScoringPipeline;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.weight.ItemTagWeightAmountPolicy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Shop-scoped demand pressure used to influence future customer order choices.
 */
public final class OrderDemandLearning {
    private static final ItemTagWeightAmountPolicy AMOUNT_POLICY = ItemTagWeightAmountPolicy.DEFAULT;
    private static final MicroPriceAdjustmentHelper<ShopId, ResourceLocation> HELPER = new MicroPriceAdjustmentHelper<>();

    private OrderDemandLearning() {
    }

    public static OrderCandidateScorer orderCandidateScorer() {
        return new DemandPressureOrderScoringPipeline(HELPER);
    }

    public static void recordSuccessfulOrderLine(ShopInstance shop, OrderLine line, long nowTick) {
        Objects.requireNonNull(shop, "shop");
        Objects.requireNonNull(line, "line");
        if (line.deliveredCount() <= 0) {
            return;
        }

        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(line.requestedItem().getItem());
        double weightAmount = AMOUNT_POLICY.unitAmountOf(line.requestedItem()) * line.deliveredCount();
        PriceAdjustmentResult result = HELPER.adjustSellPrice(new PriceAdjustmentRequest<>(
                shop.shopId(),
                itemKey,
                line.unitPrice(),
                weightAmount,
                nowTick
        ));
        learnSuccessfulDemand(result, weightAmount);
    }

    public static MicroPriceAdjustmentHelper<ShopId, ResourceLocation> helper() {
        return HELPER;
    }

    private static void learnSuccessfulDemand(PriceAdjustmentResult result, double weightAmount) {
        if (!result.modelSelected()) {
            return;
        }

        HELPER.learn(new PriceAdjustmentFeedback(
                result.afterWeight(),
                weightAmount,
                result.multiplier(),
                successfulDemandScore(result)
        ));
    }

    private static double successfulDemandScore(PriceAdjustmentResult result) {
        return Math.max(0.0D, Math.min(1.0D, 1.0D - result.multiplier()));
    }
}

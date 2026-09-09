package com.y271727uy.shopcore.core.order.delivery;

import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.integration.farmerstales.FarmersTalesQualityCompat;
import com.y271727uy.shopcore.integration.farmerstales.FarmersTalesFoodExpiryCompat;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure order fulfillment logic.
 * Inventory mutation, settlement, tax and session-stat recording are handled by callers.
 */
public final class OrderDeliveryService {
    private OrderDeliveryService() {
    }

    public static OrderDeliveryResult deliver(ShopOrder order, ItemStack input) {
        Objects.requireNonNull(input, "input");
        return deliver(order, input, input.getCount(), 0L);
    }

    public static OrderDeliveryResult deliver(ShopOrder order, ItemStack input, int maxAmount) {
        return deliver(order, input, maxAmount, 0L);
    }

    public static OrderDeliveryResult deliver(ShopOrder order, ItemStack input, int maxAmount, long gameTime) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(input, "input");
        if (maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount cannot be negative");
        }

        if (input.isEmpty() || maxAmount == 0) {
            return unchanged(OrderDeliveryStatus.INPUT_EMPTY, order, input);
        }
        if (!order.canReceiveDelivery()) {
            return unchanged(OrderDeliveryStatus.NOT_DELIVERABLE, order, input);
        }

        int remainingAmount = Math.min(input.getCount(), maxAmount);
        int consumed = 0;
        long deliveredValue = 0L;
        List<OrderLine> updatedLines = new ArrayList<>(order.lines().size());

        for (OrderLine line : order.lines()) {
            if (remainingAmount <= 0 || line.isComplete() || !line.matches(input)) {
                updatedLines.add(line);
                continue;
            }

            int accepted = Math.min(line.remainingCount(), remainingAmount);
            OrderLine updatedLine = line.deliver(accepted);
            updatedLines.add(updatedLine);
            remainingAmount -= accepted;
            consumed += accepted;
            deliveredValue += (long) accepted * line.unitPrice();
        }

        if (consumed == 0) {
            return unchanged(OrderDeliveryStatus.NO_MATCH, order, input);
        }

        FarmersTalesQualityCompat.CustomerDeliveryBonus qualityBonus = FarmersTalesQualityCompat.getCustomerDeliveryBonus(input);
        FarmersTalesFoodExpiryCompat.CustomerDeliveryBonus freshnessBonus =
                FarmersTalesFoodExpiryCompat.getCustomerDeliveryBonus(input, gameTime);
        ShopOrder afterOrder = order.withLines(updatedLines)
                .withAddedQualityBonus(
                        (long) consumed * (qualityBonus.extraPrice() + freshnessBonus.revenueAdjustment()),
                        consumed * (qualityBonus.extraReputation() + freshnessBonus.reputationAdjustment())
                )
                .refreshDeliveryStatus();
        ItemStack remainingInput = input.copy();
        remainingInput.shrink(consumed);
        OrderDeliveryStatus status = afterOrder.status() == OrderStatus.COMPLETED
                ? OrderDeliveryStatus.COMPLETED
                : OrderDeliveryStatus.ACCEPTED;
        return new OrderDeliveryResult(status, order, afterOrder, remainingInput, consumed, deliveredValue);
    }

    private static OrderDeliveryResult unchanged(OrderDeliveryStatus status, ShopOrder order, ItemStack input) {
        return new OrderDeliveryResult(status, order, order, input, 0, 0L);
    }
}

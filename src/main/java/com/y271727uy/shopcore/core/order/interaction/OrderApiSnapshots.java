package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.api.component.order.*;
import com.y271727uy.shopcore.core.order.ShopOrder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

/** The only conversion boundary between internal order models and public snapshots. */
public final class OrderApiSnapshots {
    private OrderApiSnapshots() {}
    public static OrderSnapshot order(ShopOrder order) {
        return new OrderSnapshot(order.orderId(), order.shopId().value(), order.shopPos(), order.customerType(),
                order.lines().stream().map(line -> new OrderLineSnapshot(line.requestedItem(), line.requestedCount(),
                        line.deliveredCount(), line.unitPrice())).toList(), OrderStatus.valueOf(order.status().name()),
                order.createdGameTime(), order.expiresGameTime(), order.qualityBonusPrice(), order.qualityBonusReputation());
    }
    public static DeliveryStatus status(OrderInteractionStatus status) {
        return DeliveryStatus.valueOf(status.name());
    }
    public static SettlementResult settlement(com.y271727uy.shopcore.core.order.settlement.OrderSettlementResult s) {
        return new SettlementResult(SettlementMode.valueOf(s.mode().name()), s.settled(), s.grossAmount(),
                s.netAmount(), s.taxAmount(), s.reputationReward(), s.checkoutResult(), s.currencyOperation());
    }
    public static DeliveryResult delivery(OrderDeliveryInteractionResult result, ItemStack input, MutableComponent message) {
        var detail = result.deliveryResult();
        return new DeliveryResult(status(result.status()),
                Optional.ofNullable(result.order()).map(OrderApiSnapshots::order),
                detail == null ? 0 : detail.consumedCount(), detail == null ? 0 : detail.deliveredValue(), input,
                result.settlementResult().map(OrderApiSnapshots::settlement), message);
    }
}

package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementMode;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Default delivery feedback. Translation keys and order formatting belong to the selected preset. */
public final class DeliveryMessages implements Function<OrderDeliveryInteractionResult, MutableComponent> {
    private final String prefix;
    private final Function<ShopOrder, String> describeOrder;

    public DeliveryMessages(String prefix) {
        this(prefix, DeliveryMessages::describeOrder);
    }

    public DeliveryMessages(String prefix, Function<ShopOrder, String> describeOrder) {
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        this.describeOrder = Objects.requireNonNull(describeOrder, "describeOrder");
    }

    @Override
    public MutableComponent apply(OrderDeliveryInteractionResult result) {
        return switch (result.status()) {
            case COMPLETED -> completed(result);
            case DELIVERED -> Component.translatable(prefix + ".delivered",
                    result.deliveryResult().consumedCount(), describeOrder.apply(result.order()));
            case INPUT_EMPTY -> message("input_empty");
            case ORDER_NOT_RECORDED -> message("not_recorded");
            case ORDER_NOT_ACTIVE -> message("not_active");
            case DELIVERY_NO_MATCH -> message("no_match");
            case DELIVERY_NOT_ALLOWED -> message("not_allowed");
            case DELIVERY_UNCHANGED -> message("unchanged");
            case NO_ORDER, RECORDED -> message("no_order");
        };
    }

    private MutableComponent message(String suffix) {
        return Component.translatable(prefix + "." + suffix);
    }

    private MutableComponent completed(OrderDeliveryInteractionResult result) {
        OrderSettlementResult settlement = result.settlementResult().orElseThrow();
        if (settlement.mode() == OrderSettlementMode.ACCOUNT_DEPOSIT) {
            return Component.translatable(prefix + (settlement.settled()
                            ? ".completed_account" : ".completed_account_failed"),
                    describeOrder.apply(result.order()), settlement.netAmount());
        }
        return Component.translatable(prefix + ".completed", describeOrder.apply(result.order()),
                settlement.checkoutResult().totalValue());
    }

    public static String describeOrder(ShopOrder order) {
        String shortId = order.orderId().toString().substring(0, 8);
        String lines = order.lines().stream().map(DeliveryMessages::describeLine).collect(Collectors.joining(", "));
        return "#" + shortId + " " + order.status().name().toLowerCase() + " [" + lines + "]";
    }

    private static String describeLine(OrderLine line) {
        return line.requestedCount() + "x " + line.requestedItem().getHoverName().getString()
                + " (" + line.deliveredCount() + "/" + line.requestedCount() + ")";
    }
}

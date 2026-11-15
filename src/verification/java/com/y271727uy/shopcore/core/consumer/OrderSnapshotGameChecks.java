package com.y271727uy.shopcore.core.consumer;

import com.y271727uy.shopcore.core.order.interaction.*;
import net.minecraft.network.chat.Component;
import java.util.*;

/** Invoke run() only after Minecraft/Forge has bootstrapped. Not part of the headless runner. */
public final class OrderSnapshotGameChecks {
    private static int checks;
    public static void run() {
        var source = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.APPLE, 4);
        var coreOrder = com.y271727uy.shopcore.core.order.ShopOrder.pending(
                new com.y271727uy.shopcore.core.shop.instance.ShopId(UUID.randomUUID()),
                net.minecraft.core.BlockPos.ZERO, net.minecraft.resources.ResourceLocation.parse("test:customer"),
                List.of(new com.y271727uy.shopcore.core.order.OrderLine(source, 4, 1, 7)), 10, 100);
        var snapshot = OrderApiSnapshots.order(coreOrder);
        expect(coreOrder.orderId(), snapshot.orderId(), "order identity");
        expect(110L, snapshot.expiresGameTime(), "expiration");
        expect(3, snapshot.remainingCount(), "remaining goods");
        expect(28L, snapshot.totalValue(), "base order value");
        coreOrder.lines().get(0).requestedItem().setCount(42);
        snapshot.lines().get(0).requestedItem().setCount(23);
        expect(1, snapshot.lines().get(0).requestedItem().getCount(), "item snapshot detached both directions");
        try {
            snapshot.lines().clear();
            throw new AssertionError("mutable snapshot lines");
        } catch (UnsupportedOperationException expected) { checks++; }
        var after = coreOrder.withLines(List.of(new com.y271727uy.shopcore.core.order.OrderLine(source, 4, 3, 7)))
                .refreshDeliveryStatus();
        var detail = new com.y271727uy.shopcore.core.order.delivery.OrderDeliveryResult(
                com.y271727uy.shopcore.core.order.delivery.OrderDeliveryStatus.ACCEPTED,
                coreOrder, after, source.copyWithCount(2), 2, 14);
        var partial = OrderApiSnapshots.delivery(OrderDeliveryInteractionResult.delivered(after, detail),
                source, Component.literal("partial"));
        source.setCount(40);
        partial.remainingInput().setCount(30);
        expect(4, partial.remainingInput().getCount(), "held input snapshot detached both directions");
        expect(2, partial.deliveredCount(), "current delivery quantity");
        expect(14L, partial.deliveredValue(), "current delivery value");
        expect(3, partial.order().orElseThrow().deliveredCount(), "cumulative delivery quantity");
        expect(Optional.empty(), partial.settlement(), "partial order has no payment");
    }
    private static void expect(Object expected, Object actual, String message) {
        checks++;
        if (!Objects.equals(expected, actual)) throw new AssertionError(message);
    }
}

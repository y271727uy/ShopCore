package com.y271727uy.shopcore.core.consumer;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.api.component.economic.CheckoutResult;
import com.y271727uy.shopcore.core.economic.tax.Tax;
import com.y271727uy.shopcore.core.order.interaction.*;
import com.y271727uy.shopcore.core.order.settlement.*;
import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/** Headless guards/feedback regression; it does not simulate inventory or bank services. */
public final class DeliveryInteractionRegression {
    private static int checks;

    public static void main(String[] args) {
        ConsumerActor actor = new ConsumerActor() {
            public UUID consumerId() { return new UUID(0, 1); }
            public Vec3 position() { return Vec3.ZERO; }
            public boolean isAlive() { return true; }
        };
        Memory memory = new Memory();
        for (QueueConsumerPhase phase : QueueConsumerPhase.values()) {
            memory.phase = phase;
            memory.order = null;
            expect(Optional.of(OrderInteractionStatus.INPUT_EMPTY),
                    CustomerDeliveryEligibility.rejection(true, actor, memory), "empty input takes precedence");
            expect(Optional.of(OrderInteractionStatus.NO_ORDER),
                    CustomerDeliveryEligibility.rejection(false, actor, memory), "missing binding takes precedence");
            memory.order = new UUID(0, 2);
            expect(phase == QueueConsumerPhase.WAITING_FOR_ORDER
                            ? Optional.of(OrderInteractionStatus.ORDER_NOT_RECORDED) : Optional.empty(),
                    CustomerDeliveryEligibility.rejection(false, actor, memory), "original phase eligibility " + phase);
            expect(phase, memory.phase, "eligibility must not transition consumer");
        }
        memory.phase = null;
        expect(Optional.empty(), CustomerDeliveryEligibility.rejection(false, actor, memory), "original unset phase behavior");

        DeliveryMessages messages = new DeliveryMessages("test.shopcore.delivery");
        Map<OrderInteractionStatus, String> failures = Map.of(
                OrderInteractionStatus.INPUT_EMPTY, "input_empty",
                OrderInteractionStatus.NO_ORDER, "no_order",
                OrderInteractionStatus.RECORDED, "no_order",
                OrderInteractionStatus.ORDER_NOT_RECORDED, "not_recorded",
                OrderInteractionStatus.ORDER_NOT_ACTIVE, "not_active",
                OrderInteractionStatus.DELIVERY_NO_MATCH, "no_match",
                OrderInteractionStatus.DELIVERY_NOT_ALLOWED, "not_allowed",
                OrderInteractionStatus.DELIVERY_UNCHANGED, "unchanged");
        failures.forEach((status, suffix) -> expect("test.shopcore.delivery." + suffix,
                contents(messages.apply(OrderDeliveryInteractionResult.failed(status))).getKey(), "failure feedback " + status));
        // Formatter doesn't need item registries when the shop supplies its own order summary.
        DeliveryMessages custom = new DeliveryMessages("custom.delivery", order -> "order-summary");
        for (OrderSettlementMode mode : List.of(OrderSettlementMode.ACCOUNT_DEPOSIT, OrderSettlementMode.CURRENCY_ITEMS)) {
            for (boolean settled : List.of(true, false)) {
                Tax.TaxResult tax = Tax.calculate(2000, mode == OrderSettlementMode.CURRENCY_ITEMS);
                OrderSettlementResult settlement = new OrderSettlementResult(mode, settled,
                        tax.grossAmount(), tax.netAmount(), tax.taxAmount(), 0, tax,
                        new CheckoutResult(List.of(), List.of(), tax.netAmount(), 0), Optional.empty(), BusinessSessionStats.empty());
                // Direct DTO construction isolates formatting from order fulfillment and Minecraft registries.
                OrderDeliveryInteractionResult result = new OrderDeliveryInteractionResult(
                        OrderInteractionStatus.COMPLETED, null, null, Optional.of(settlement));
                TranslatableContents rendered = contents(custom.apply(result));
                String suffix = mode == OrderSettlementMode.ACCOUNT_DEPOSIT
                        ? (settled ? "completed_account" : "completed_account_failed") : "completed";
                expect("custom.delivery." + suffix, rendered.getKey(), "settlement feedback branch");
                expect("order-summary", rendered.getArgs()[0], "custom order formatter");
                expect(tax.netAmount(), rendered.getArgs()[1], "payout argument");
            }
        }
        structuredResults();
        System.out.println("Delivery interaction guards and feedback passed: " + checks + " checks");
    }

    private static void structuredResults() {
        for (var status : OrderInteractionStatus.values()) {
            expect(status.name(), OrderApiSnapshots.status(status).name(), "public status preserved");
        }
        for (boolean settled : List.of(true, false)) {
            var tax = Tax.calculate(2000, false);
            var operation = settled
                    ? com.y271727uy.shopcore.api.component.economic.CurrencyOperationResult.success(1900, OptionalDouble.empty(), "ok")
                    : com.y271727uy.shopcore.api.component.economic.CurrencyOperationResult.failure(1900, "bank unavailable");
            var settlement = new OrderSettlementResult(OrderSettlementMode.ACCOUNT_DEPOSIT, settled,
                    2000, 1900, 100, 3, tax, new CheckoutResult(List.of(), List.of(), 1900, 3),
                    Optional.of(operation), BusinessSessionStats.empty());
            var payment = OrderApiSnapshots.settlement(settlement);
            expect(settled, payment.settled(), "bank outcome preserved");
            expect(2000L, payment.grossAmount(), "gross preserved");
            expect(1900L, payment.netAmount(), "net preserved");
            expect(100L, payment.taxAmount(), "tax preserved");
            expect(Optional.of(operation), payment.currencyOperation(), "bank detail preserved");
        }
    }

    private static TranslatableContents contents(Component component) {
        return (TranslatableContents) component.getContents();
    }

    private static void expect(Object expected, Object actual, String message) {
        checks++;
        if (!Objects.equals(expected, actual)) throw new AssertionError(message + ": " + expected + " != " + actual);
    }

    private static final class Memory implements ConsumerMemory {
        QueueConsumerPhase phase;
        UUID order;
        public QueueConsumerPhase phase(ConsumerActor a) { return phase; }
        public Optional<UUID> orderId(ConsumerActor a) { return Optional.ofNullable(order); }
        public void setPhase(ConsumerActor a, QueueConsumerPhase p) { throw new AssertionError("guard mutated phase"); }
        public long joinedGameTime(ConsumerActor a) { throw new AssertionError("unexpected arrival read"); }
        public void setJoinedGameTime(ConsumerActor a, long t) { throw new AssertionError("guard mutated arrival"); }
        public void setOrderId(ConsumerActor a, UUID id) { throw new AssertionError("guard mutated order"); }
        public void clearOrderId(ConsumerActor a) { throw new AssertionError("guard cleared order"); }
        public Optional<Vec3> departureTarget(ConsumerActor a) { throw new AssertionError("unexpected departure read"); }
        public void setDepartureTarget(ConsumerActor a, Vec3 p) { throw new AssertionError("guard mutated target"); }
        public void clearDepartureTarget(ConsumerActor a) { throw new AssertionError("guard cleared target"); }
    }
}

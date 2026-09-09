package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.delivery.OrderDeliveryResult;
import com.y271727uy.shopcore.core.order.delivery.OrderDeliveryService;
import com.y271727uy.shopcore.core.order.evaluator.OrderEvaluation;
import com.y271727uy.shopcore.core.order.prompt.OrderPrompt;
import com.y271727uy.shopcore.core.order.prompt.OrderPromptFactory;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementBinding;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementBridge;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementBridgeContext;
import com.y271727uy.shopcore.core.order.settlement.OrderSettlementResult;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeHolder;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper.OrderDemandLearning;
import com.y271727uy.shopcore.integration.farmerstales.FarmersTalesFoodExpiryCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class OrderInteractionService {
    private OrderInteractionService() {
    }

    public static <A extends ConsumerActor> OrderRecordResult recordNextWaitingOrder(
            ServerPlayer player,
            List<A> actors,
            ConsumerMemory memory,
            ShopOrderBook orderBook,
            OrderPromptSink promptSink
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(orderBook, "orderBook");
        Objects.requireNonNull(promptSink, "promptSink");

        List<A> waitingActors = Objects.requireNonNull(actors, "actors").stream()
                .filter(actor -> memory.phase(actor) == QueueConsumerPhase.WAITING_FOR_ORDER)
                .sorted(Comparator.comparingLong(memory::joinedGameTime).thenComparing(ConsumerActor::consumerId))
                .toList();
        for (A actor : waitingActors) {
            Optional<ShopOrder> order = activeOrderFor(actor, memory, orderBook);
            if (order.isEmpty()) {
                memory.setPhase(actor, QueueConsumerPhase.LEAVING);
                continue;
            }

            OrderPrompt prompt = OrderPromptFactory.fromOrder(order.get());
            promptSink.show(player, prompt);
            memory.setPhase(actor, QueueConsumerPhase.WAITING_FOR_DELIVERY);
            return OrderRecordResult.recorded(order.get());
        }
        return OrderRecordResult.failed(OrderInteractionStatus.NO_ORDER);
    }

    public static OrderDeliveryInteractionResult deliverToRecordedCustomer(
            ServerPlayer player,
            ConsumerActor actor,
            ItemStack input,
            ConsumerMemory memory,
            ShopBlockRuntimeHolder holder,
            OrderPromptSink promptSink,
            OrderPromptClearer promptClearer
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(promptSink, "promptSink");
        Objects.requireNonNull(promptClearer, "promptClearer");

        if (input.isEmpty()) {
            return OrderDeliveryInteractionResult.failed(OrderInteractionStatus.INPUT_EMPTY);
        }
        if (memory.phase(actor) == QueueConsumerPhase.WAITING_FOR_ORDER) {
            return OrderDeliveryInteractionResult.failed(OrderInteractionStatus.ORDER_NOT_RECORDED);
        }

        ShopOrderBook orderBook = holder.shopcore$orderBook();
        Optional<ShopOrder> orderOptional = memory.orderId(actor)
                .flatMap(orderBook::find)
                .filter(ShopOrder::canReceiveDelivery);
        if (orderOptional.isEmpty()) {
            memory.setPhase(actor, QueueConsumerPhase.LEAVING);
            return OrderDeliveryInteractionResult.failed(OrderInteractionStatus.ORDER_NOT_ACTIVE);
        }

        long gameTime = player.level().getGameTime();
        boolean deliveredRottenFood = FarmersTalesFoodExpiryCompat.getFreshness(input, gameTime)
                == FarmersTalesFoodExpiryCompat.Freshness.ROTTEN;
        OrderDeliveryResult deliveryResult = OrderDeliveryService.deliver(orderOptional.get(), input, input.getCount(), gameTime);
        if (!deliveryResult.changed()) {
            return switch (deliveryResult.deliveryStatus()) {
                case INPUT_EMPTY -> OrderDeliveryInteractionResult.failed(OrderInteractionStatus.INPUT_EMPTY);
                case NO_MATCH -> OrderDeliveryInteractionResult.failed(OrderInteractionStatus.DELIVERY_NO_MATCH);
                case NOT_DELIVERABLE -> OrderDeliveryInteractionResult.failed(OrderInteractionStatus.DELIVERY_NOT_ALLOWED);
                case ACCEPTED, COMPLETED -> OrderDeliveryInteractionResult.failed(OrderInteractionStatus.DELIVERY_UNCHANGED);
            };
        }

        ShopOrder afterOrder = deliveryResult.afterOrder();
        holder.shopcore$setOrderBook(orderBook.replace(afterOrder));
        if (!player.getAbilities().instabuild) {
            input.setCount(deliveryResult.remainingInput().getCount());
        }
        if (deliveredRottenFood) {
            player.sendSystemMessage(Component.translatable(player.getRandom().nextBoolean()
                    ? "message.shopcore.customer_rotten_food_angry"
                    : "message.shopcore.customer_rotten_food_sick"));
        }

        if (deliveryResult.completedOrder()) {
            OrderSettlementResult settlement = settleCompletedOrder(player, holder, afterOrder);
            memory.setPhase(actor, QueueConsumerPhase.LEAVING);
            memory.clearOrderId(actor);
            promptClearer.clear(player);
            return OrderDeliveryInteractionResult.completed(afterOrder, deliveryResult, settlement);
        }

        promptSink.show(player, OrderPromptFactory.fromOrder(afterOrder));
        return OrderDeliveryInteractionResult.delivered(afterOrder, deliveryResult);
    }

    private static Optional<ShopOrder> activeOrderFor(
            ConsumerActor actor,
            ConsumerMemory memory,
            ShopOrderBook orderBook
    ) {
        return memory.orderId(actor)
                .flatMap(orderBook::find)
                .filter(ShopOrder::canReceiveDelivery);
    }

    private static OrderSettlementResult settleCompletedOrder(
            ServerPlayer player,
            ShopBlockRuntimeHolder holder,
            ShopOrder completedOrder
    ) {
        ShopInstance shop = holder.shopcore$shopInstance();
        learnSuccessfulOrderDemand(completedOrder, shop, player.level().getGameTime());
        OrderEvaluation evaluation = OrderEvaluation.of(
                completedOrder.asItemListRequest().kind(),
                completedOrder.totalDeliveredCount(),
                completedOrder.totalRequestedCount(),
                100.0D,
                OrderEvaluation.REASON_ACCEPTED
        );
        OrderSettlementResult settlement = OrderSettlementBridge.settle(new OrderSettlementBridgeContext(
                shop,
                completedOrder,
                evaluation,
                OrderSettlementBinding.currencyItems(),
                0.0D
        ));
        holder.shopcore$setShopInstance(shop.withCurrentSession(settlement.sessionStats()));
        giveCurrencyReward(player, settlement);
        return settlement;
    }

    private static void learnSuccessfulOrderDemand(ShopOrder completedOrder, ShopInstance shop, long nowTick) {
        for (OrderLine line : completedOrder.lines()) {
            OrderDemandLearning.recordSuccessfulOrderLine(shop, line, nowTick);
        }
    }

    private static void giveCurrencyReward(ServerPlayer player, OrderSettlementResult settlement) {
        for (ItemStack reward : settlement.checkoutResult().currencyReward()) {
            ItemStack remaining = reward.copy();
            if (!player.getInventory().add(remaining)) {
                player.drop(remaining, false);
            }
        }
    }
}

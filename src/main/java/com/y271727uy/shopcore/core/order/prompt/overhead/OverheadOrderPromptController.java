package com.y271727uy.shopcore.core.order.prompt.overhead;

import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.prompt.OrderPromptFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class OverheadOrderPromptController {
    public static final String REVEAL_GAME_TIME_TAG = "ShopCoreOverheadOrderRevealGameTime";

    private final long waitTicks;
    private final Component waitingText;

    public OverheadOrderPromptController(long waitTicks, Component waitingText) {
        if (waitTicks < 0L) {
            throw new IllegalArgumentException("waitTicks cannot be negative");
        }
        this.waitTicks = waitTicks;
        this.waitingText = Objects.requireNonNull(waitingText, "waitingText");
    }

    public <A extends ConsumerActor & EntityBackedConsumerActor> void tickWaitingConsumers(
            List<A> actors,
            ConsumerMemory memory,
            ShopOrderBook orderBook,
            long gameTime
    ) {
        Objects.requireNonNull(actors, "actors");
        Objects.requireNonNull(memory, "memory");
        Objects.requireNonNull(orderBook, "orderBook");
        for (A actor : actors) {
            if (memory.phase(actor) != QueueConsumerPhase.WAITING_FOR_ORDER) {
                continue;
            }
            Optional<UUID> orderId = memory.orderId(actor);
            if (orderId.isEmpty()) {
                continue;
            }
            Optional<ShopOrder> order = orderBook.find(orderId.get()).filter(ShopOrder::canReceiveDelivery);
            if (order.isEmpty()) {
                continue;
            }

            Entity entity = actor.entity();
            CompoundTag data = entity.getPersistentData();
            if (!data.contains(REVEAL_GAME_TIME_TAG)) {
                data.putLong(REVEAL_GAME_TIME_TAG, gameTime + waitTicks);
                CustomerOverheadOrderPromptService.showWaiting(entity, waitingText);
                continue;
            }
            if (gameTime < data.getLong(REVEAL_GAME_TIME_TAG)) {
                CustomerOverheadOrderPromptService.showWaiting(entity, waitingText);
                continue;
            }

            CustomerOverheadOrderPromptService.show(entity, OrderPromptFactory.fromOrder(order.get()));
            data.remove(REVEAL_GAME_TIME_TAG);
            memory.setPhase(actor, QueueConsumerPhase.WAITING_FOR_DELIVERY);
        }
    }

    public void clear(Entity entity) {
        Objects.requireNonNull(entity, "entity");
        entity.getPersistentData().remove(REVEAL_GAME_TIME_TAG);
        CustomerOverheadOrderPromptService.clear(entity);
    }
}

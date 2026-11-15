package com.y271727uy.shopcore.core.order.prompt.overhead;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.taking.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import java.util.List;
/** Compatibility facade. Timing and phase transitions now belong to the order-taking behavior. */
public class OverheadOrderPromptController {
    public static final String REVEAL_GAME_TIME_TAG = EntityOrderTakingMemory.REVEAL_GAME_TIME_TAG;
    private final OrderTakingController<EntityBackedConsumerActor> controller;
    public OverheadOrderPromptController(long waitTicks, Component waitingText) {
        controller = new OrderTakingController<>(new AutomaticOrderTaking(waitTicks, new EntityOrderTakingMemory()),
                new OverheadOrderTakingPresentation<>(waitingText));
    }
    public <A extends ConsumerActor & EntityBackedConsumerActor> void tickWaitingConsumers(
            List<A> actors, ConsumerMemory memory, ShopOrderBook orderBook, long gameTime) {
        controller.tick(List.copyOf(actors), memory, orderBook, gameTime);
    }
    public void clear(Entity entity) { controller.clear(new EntityConsumerActor(entity)); }
}

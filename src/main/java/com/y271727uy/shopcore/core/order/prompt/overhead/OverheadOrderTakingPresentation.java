package com.y271727uy.shopcore.core.order.prompt.overhead;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityBackedConsumerActor;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.prompt.OrderPromptFactory;
import com.y271727uy.shopcore.core.order.taking.OrderTakingPresentation;
import net.minecraft.network.chat.Component;
import java.util.Objects;
public final class OverheadOrderTakingPresentation<A extends EntityBackedConsumerActor> implements OrderTakingPresentation<A> {
    private final Component waitingText;
    public OverheadOrderTakingPresentation(Component waitingText) {
        this.waitingText = Objects.requireNonNull(waitingText, "waitingText");
    }
    public void waiting(A actor) { CustomerOverheadOrderPromptService.showWaiting(actor.entity(), waitingText); }
    public void accepted(A actor, ShopOrder order) {
        CustomerOverheadOrderPromptService.show(actor.entity(), OrderPromptFactory.fromOrder(order));
    }
    public void clear(A actor) { CustomerOverheadOrderPromptService.clear(actor.entity()); }
}

package com.y271727uy.shopcore.core.order.interaction;

import com.y271727uy.shopcore.core.consumer.common.ConsumerMemory;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeHolder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** The existing hand-delivery flow, assembled once per shop with optional gameplay feedback. */
public final class HeldItemDeliveryInteraction {
    private final ShopBlockRuntimeHolder holder;
    private final ConsumerMemory memory;
    private final Runnable initialize;
    private final Runnable changed;
    private final CustomerOrderPresentation presentation;
    private final Function<OrderDeliveryInteractionResult, MutableComponent> messages;
    private final BiConsumer<ServerPlayer, DeliveryReceipt> onDelivered;

    public HeldItemDeliveryInteraction(ShopBlockRuntimeHolder holder, ConsumerMemory memory,
            Runnable initialize, Runnable changed, CustomerOrderPresentation presentation,
            Function<OrderDeliveryInteractionResult, MutableComponent> messages,
            BiConsumer<ServerPlayer, DeliveryReceipt> onDelivered) {
        this.holder = Objects.requireNonNull(holder, "holder");
        this.memory = Objects.requireNonNull(memory, "memory");
        this.initialize = Objects.requireNonNull(initialize, "initialize");
        this.changed = Objects.requireNonNull(changed, "changed");
        this.presentation = Objects.requireNonNull(presentation, "presentation");
        this.messages = Objects.requireNonNull(messages, "messages");
        this.onDelivered = Objects.requireNonNull(onDelivered, "onDelivered");
    }

    public com.y271727uy.shopcore.api.component.order.DeliveryResult deliver(ServerPlayer player, Entity customer, ItemStack input) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(input, "input");
        initialize.run();
        EntityConsumerActor actor = new EntityConsumerActor(customer);
        // Keep the original guard precedence and early-return behavior.
        Optional<OrderInteractionStatus> rejection = CustomerDeliveryEligibility.rejection(input.isEmpty(), actor, memory);
        if (rejection.isPresent()) {
            var result = OrderDeliveryInteractionResult.failed(rejection.get());
            return OrderApiSnapshots.delivery(result, input, messages.apply(result));
        }

        ItemStack inputBefore = input.copy();
        OrderDeliveryInteractionResult result = OrderInteractionService.deliverToRecordedCustomer(
                player, actor, input, memory, holder,
                (recipient, prompt) -> presentation.show(recipient, customer, prompt),
                recipient -> presentation.clear(recipient, customer), changed);
        DeliveryReceipt receipt = new DeliveryReceipt(inputBefore, result);
        // The order commit has already requested persistence before optional gameplay feedback runs.
        if (receipt.delivered()) {
            onDelivered.accept(player, receipt);
        }
        return OrderApiSnapshots.delivery(result, input, messages.apply(result));
    }
}

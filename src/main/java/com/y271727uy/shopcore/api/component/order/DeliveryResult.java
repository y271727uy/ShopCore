package com.y271727uy.shopcore.api.component.order;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import java.util.Objects;
import java.util.Optional;

/** Result of a delivery command. Input is consumed except in creative mode; remainingInput snapshots the actual held stack.
 * An absent settlement means no settlement was attempted. message is presentation that callers may choose to display.
 */
public record DeliveryResult(DeliveryStatus status, Optional<OrderSnapshot> order, int deliveredCount,
        long deliveredValue, ItemStack remainingInput, Optional<SettlementResult> settlement,
        MutableComponent message) {
    public DeliveryResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(settlement, "settlement");
        remainingInput = Objects.requireNonNull(remainingInput, "remainingInput").copy();
        message = Objects.requireNonNull(message, "message").copy();
    }
    @Override public ItemStack remainingInput() { return remainingInput.copy(); }
    @Override public MutableComponent message() { return message.copy(); }
    public boolean delivered() { return deliveredCount > 0; }
}

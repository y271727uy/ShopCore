package com.y271727uy.shopcore.core.order.delivery;

import com.y271727uy.shopcore.core.order.ShopOrder;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record OrderDeliveryResult(
        OrderDeliveryStatus deliveryStatus,
        ShopOrder beforeOrder,
        ShopOrder afterOrder,
        ItemStack remainingInput,
        int consumedCount,
        long deliveredValue
) {
    public OrderDeliveryResult {
        Objects.requireNonNull(deliveryStatus, "deliveryStatus");
        Objects.requireNonNull(beforeOrder, "beforeOrder");
        Objects.requireNonNull(afterOrder, "afterOrder");
        Objects.requireNonNull(remainingInput, "remainingInput");
        remainingInput = remainingInput.copy();
        if (consumedCount < 0) {
            throw new IllegalArgumentException("consumedCount cannot be negative");
        }
        if (deliveredValue < 0L) {
            throw new IllegalArgumentException("deliveredValue cannot be negative");
        }
    }

    public boolean changed() {
        return consumedCount > 0;
    }

    public boolean completedOrder() {
        return deliveryStatus == OrderDeliveryStatus.COMPLETED;
    }
}

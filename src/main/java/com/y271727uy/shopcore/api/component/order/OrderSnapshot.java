package com.y271727uy.shopcore.api.component.order;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Point-in-time order data. Values exclude quality bonuses unless explicitly named. */
public record OrderSnapshot(UUID orderId, UUID shopId, BlockPos shopPos, ResourceLocation customerType,
        List<OrderLineSnapshot> lines, OrderStatus status, long createdGameTime, long expiresGameTime,
        long qualityBonusPrice, double qualityBonusReputation) {
    public OrderSnapshot {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(shopId, "shopId");
        shopPos = Objects.requireNonNull(shopPos, "shopPos").immutable();
        Objects.requireNonNull(customerType, "customerType");
        lines = List.copyOf(lines);
        Objects.requireNonNull(status, "status");
    }
    public boolean active() { return status == OrderStatus.PENDING || status == OrderStatus.PARTIAL; }
    public int requestedCount() { return lines.stream().mapToInt(OrderLineSnapshot::requestedCount).sum(); }
    public int deliveredCount() { return lines.stream().mapToInt(OrderLineSnapshot::deliveredCount).sum(); }
    public int remainingCount() { return requestedCount() - deliveredCount(); }
    public long totalValue() { return lines.stream().mapToLong(OrderLineSnapshot::totalValue).sum(); }
}

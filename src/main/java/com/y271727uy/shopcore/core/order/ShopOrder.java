package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.order.request.ItemListOrderRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ShopOrder(
        UUID orderId,
        ShopId shopId,
        BlockPos shopPos,
        ResourceLocation customerType,
        List<OrderLine> lines,
        OrderStatus status,
        long createdGameTime,
        long expiresGameTime
) {
    public ShopOrder {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(shopPos, "shopPos");
        Objects.requireNonNull(customerType, "customerType");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines cannot be empty");
        }
        Objects.requireNonNull(status, "status");
        if (createdGameTime < 0L) {
            throw new IllegalArgumentException("createdGameTime cannot be negative");
        }
        if (expiresGameTime < createdGameTime) {
            throw new IllegalArgumentException("expiresGameTime cannot be before createdGameTime");
        }
    }

    public static ShopOrder pending(
            ShopId shopId,
            BlockPos shopPos,
            ResourceLocation customerType,
            List<OrderLine> lines,
            long createdGameTime,
            long ttlTicks
    ) {
        return new ShopOrder(
                UUID.randomUUID(),
                shopId,
                shopPos,
                customerType,
                lines,
                OrderStatus.PENDING,
                createdGameTime,
                createdGameTime + Math.max(0L, ttlTicks)
        );
    }

    public long totalValue() {
        return lines.stream().mapToLong(OrderLine::totalValue).sum();
    }

    public int totalRequestedCount() {
        return lines.stream().mapToInt(OrderLine::requestedCount).sum();
    }

    public ItemListOrderRequest asItemListRequest() {
        return new ItemListOrderRequest(lines);
    }

    public int totalDeliveredCount() {
        return lines.stream().mapToInt(OrderLine::deliveredCount).sum();
    }

    public int remainingCount() {
        return lines.stream().mapToInt(OrderLine::remainingCount).sum();
    }

    public long deliveredValue() {
        return lines.stream().mapToLong(OrderLine::deliveredValue).sum();
    }

    public boolean isCompleted() {
        return lines.stream().allMatch(OrderLine::isComplete);
    }

    public boolean isExpired(long gameTime) {
        return status != OrderStatus.COMPLETED && status != OrderStatus.CANCELLED && gameTime >= expiresGameTime;
    }

    public boolean canReceiveDelivery() {
        return status == OrderStatus.PENDING || status == OrderStatus.PARTIAL;
    }

    public ShopOrder withLines(List<OrderLine> lines) {
        return new ShopOrder(orderId, shopId, shopPos, customerType, lines, status, createdGameTime, expiresGameTime);
    }

    public ShopOrder withStatus(OrderStatus status) {
        return new ShopOrder(orderId, shopId, shopPos, customerType, lines, status, createdGameTime, expiresGameTime);
    }

    public ShopOrder refreshDeliveryStatus() {
        if (status == OrderStatus.EXPIRED || status == OrderStatus.CANCELLED) {
            return this;
        }
        if (isCompleted()) {
            return withStatus(OrderStatus.COMPLETED);
        }
        if (totalDeliveredCount() > 0) {
            return withStatus(OrderStatus.PARTIAL);
        }
        return withStatus(OrderStatus.PENDING);
    }
}

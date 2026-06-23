package com.y271727uy.shopcore.core.consumer.common.wait;

import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutRole;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutSlot;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutTransform;
import net.minecraft.core.Direction;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record QueueConsumerLayout(
        ConsumerLayoutTransform transform,
        ConsumerLayoutSlot serviceSlot,
        List<ConsumerLayoutSlot> queueSlots,
        ConsumerLayoutSlot exitSlot,
        Direction serviceFacing,
        double moveSpeed,
        double arrivalDistance
) {
    public QueueConsumerLayout {
        Objects.requireNonNull(transform, "transform");
        Objects.requireNonNull(serviceSlot, "serviceSlot");
        if (serviceSlot.role() != ConsumerLayoutRole.SERVICE) {
            throw new IllegalArgumentException("serviceSlot must have SERVICE role");
        }
        queueSlots = List.copyOf(Objects.requireNonNull(queueSlots, "queueSlots").stream()
                .sorted(Comparator.comparingInt(ConsumerLayoutSlot::index))
                .toList());
        for (ConsumerLayoutSlot slot : queueSlots) {
            if (slot.role() != ConsumerLayoutRole.QUEUE) {
                throw new IllegalArgumentException("queue slot must have QUEUE role");
            }
        }
        Objects.requireNonNull(exitSlot, "exitSlot");
        if (exitSlot.role() != ConsumerLayoutRole.EXIT) {
            throw new IllegalArgumentException("exitSlot must have EXIT role");
        }
        Objects.requireNonNull(serviceFacing, "serviceFacing");
        if (serviceFacing.getAxis().isVertical()) {
            throw new IllegalArgumentException("serviceFacing must be horizontal");
        }
        if (!Double.isFinite(moveSpeed) || moveSpeed <= 0.0D) {
            throw new IllegalArgumentException("moveSpeed must be positive");
        }
        if (!Double.isFinite(arrivalDistance) || arrivalDistance <= 0.0D) {
            throw new IllegalArgumentException("arrivalDistance must be positive");
        }
    }

    public ConsumerLayoutSlot queueSlotForIndex(int queueIndex) {
        if (queueSlots.isEmpty()) {
            return serviceSlot;
        }
        return queueSlots.get(Math.min(Math.max(0, queueIndex), queueSlots.size() - 1));
    }
}

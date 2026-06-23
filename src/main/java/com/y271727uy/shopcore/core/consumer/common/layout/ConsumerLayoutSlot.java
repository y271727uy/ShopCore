package com.y271727uy.shopcore.core.consumer.common.layout;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public record ConsumerLayoutSlot(
        ConsumerLayoutRole role,
        int index,
        BlockPos localOffset
) {
    public ConsumerLayoutSlot {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(localOffset, "localOffset");
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be negative");
        }
    }
}

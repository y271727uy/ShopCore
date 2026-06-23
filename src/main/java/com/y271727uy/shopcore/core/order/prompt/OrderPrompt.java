package com.y271727uy.shopcore.core.order.prompt;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OrderPrompt(
        UUID orderId,
        List<OrderPromptLine> lines
) {
    public OrderPrompt {
        Objects.requireNonNull(orderId, "orderId");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("lines cannot be empty");
        }
    }
}

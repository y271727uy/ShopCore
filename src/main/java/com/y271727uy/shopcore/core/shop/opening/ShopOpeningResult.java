package com.y271727uy.shopcore.core.shop.opening;

import java.util.List;
import java.util.Objects;

public record ShopOpeningResult(
        List<ShopOpeningFailure> failures
) {
    public ShopOpeningResult {
        failures = List.copyOf(Objects.requireNonNull(failures, "failures"));
    }

    public static ShopOpeningResult allowed() {
        return new ShopOpeningResult(List.of());
    }

    public static ShopOpeningResult failed(ShopOpeningFailure failure) {
        return new ShopOpeningResult(List.of(Objects.requireNonNull(failure, "failure")));
    }

    public boolean allowedToOpen() {
        return failures.isEmpty();
    }

    public ShopOpeningResult merge(ShopOpeningResult other) {
        Objects.requireNonNull(other, "other");
        if (failures.isEmpty()) {
            return other;
        }
        if (other.failures().isEmpty()) {
            return this;
        }
        java.util.ArrayList<ShopOpeningFailure> merged = new java.util.ArrayList<>(failures);
        merged.addAll(other.failures());
        return new ShopOpeningResult(merged);
    }
}

package com.y271727uy.shopcore.core.order.generation;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.diagnostic.ShopDiagnostic;
import com.y271727uy.shopcore.core.shop.diagnostic.ShopDiagnosticCode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ShopOrderGenerationResult(
        ShopOrderGenerationStatus status,
        ShopOrder order,
        List<ShopListing> resolvedListings,
        ShopDiagnostic diagnostic
) {
    public ShopOrderGenerationResult {
        Objects.requireNonNull(status, "status");
        resolvedListings = List.copyOf(Objects.requireNonNull(resolvedListings, "resolvedListings"));
        Objects.requireNonNull(diagnostic, "diagnostic");
    }

    public static ShopOrderGenerationResult generated(ShopOrder order, List<ShopListing> resolvedListings) {
        return new ShopOrderGenerationResult(
                ShopOrderGenerationStatus.GENERATED,
                Objects.requireNonNull(order, "order"),
                resolvedListings,
                ShopDiagnostic.of(ShopDiagnosticCode.ORDER_GENERATED)
        );
    }

    public static ShopOrderGenerationResult empty(ShopOrderGenerationStatus status, List<ShopListing> resolvedListings, ShopDiagnosticCode code) {
        if (status == ShopOrderGenerationStatus.GENERATED) {
            throw new IllegalArgumentException("generated status requires an order");
        }
        return new ShopOrderGenerationResult(status, null, resolvedListings, ShopDiagnostic.of(code));
    }

    public Optional<ShopOrder> orderOptional() {
        return Optional.ofNullable(order);
    }

    public boolean generatedOrder() {
        return status == ShopOrderGenerationStatus.GENERATED && order != null;
    }
}

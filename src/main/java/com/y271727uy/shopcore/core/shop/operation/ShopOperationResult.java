package com.y271727uy.shopcore.core.shop.operation;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyDecision;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ShopOperationResult(
        ShopOperationStatus status,
        ShopInstance shop,
        List<ShopListing> resolvedListings,
        ShopOpeningResult openingResult,
        OperatingPolicyDecision policyDecision,
        String reason
) {
    public static final String REASON_OPEN_REQUEST_DISABLED = "open_request_disabled";
    public static final String REASON_ALREADY_CLOSED = "already_closed";
    public static final String REASON_ALREADY_OPEN = "already_open";
    public static final String REASON_OPENED = "opened";
    public static final String REASON_EMPTY_MENU = "empty_menu";
    public static final String REASON_NO_RESOLVED_LISTINGS = "no_resolved_listings";
    public static final String REASON_NO_ITEM_CANDIDATES = "no_item_candidates";
    public static final String REASON_ORDER_CAPACITY_FULL = "order_capacity_full";

    public ShopOperationResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(shop, "shop");
        resolvedListings = List.copyOf(Objects.requireNonNull(resolvedListings, "resolvedListings"));
        reason = Objects.requireNonNull(reason, "reason").trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("reason cannot be blank");
        }
    }

    public boolean allowed() {
        return status == ShopOperationStatus.OPENED
                || status == ShopOperationStatus.ALREADY_OPEN
                || status == ShopOperationStatus.CLOSED
                || status == ShopOperationStatus.ALREADY_CLOSED;
    }

    public Optional<ShopOpeningResult> openingResultOptional() {
        return Optional.ofNullable(openingResult);
    }

    public Optional<OperatingPolicyDecision> policyDecisionOptional() {
        return Optional.ofNullable(policyDecision);
    }

    static ShopOperationResult of(
            ShopOperationStatus status,
            ShopInstance shop,
            List<ShopListing> resolvedListings,
            ShopOpeningResult openingResult,
            OperatingPolicyDecision policyDecision,
            String reason
    ) {
        return new ShopOperationResult(status, shop, resolvedListings, openingResult, policyDecision, reason);
    }
}

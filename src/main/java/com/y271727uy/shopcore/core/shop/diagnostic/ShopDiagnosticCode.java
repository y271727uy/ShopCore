package com.y271727uy.shopcore.core.shop.diagnostic;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.shop.operation.ShopOperationResult;
import com.y271727uy.shopcore.core.shop.operation.ShopOperationStatus;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public enum ShopDiagnosticCode {
    NONE("none"),
    OPENED("opened"),
    CLOSED("closed"),
    ALREADY_OPEN("already_open"),
    ALREADY_CLOSED("already_closed"),
    OPEN_REQUEST_DISABLED("open_request_disabled"),
    MENU_EMPTY("menu_empty"),
    NO_RESOLVED_LISTINGS("no_resolved_listings"),
    NO_ITEM_CANDIDATES("no_item_candidates"),
    OPENING_RULE_FAILED("opening_rule_failed"),
    POLICY_CLOSED("policy_closed"),
    ORDER_CAPACITY_FULL("order_capacity_full"),
    ORDER_GENERATED("order_generated"),
    ORDER_GENERATION_SKIPPED("order_generation_skipped"),
    ORDER_GENERATION_NO_LISTINGS("order_generation_no_listings"),
    ORDER_GENERATION_NO_CANDIDATE("order_generation_no_candidate"),
    ORDER_GENERATION_CLOSED("order_generation_closed"),
    RUNTIME_TICKED("runtime_ticked");

    private final ResourceLocation id;
    private final String translationKey;

    ShopDiagnosticCode(String path) {
        this.id = ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, path);
        this.translationKey = "diagnostic." + ShopcoreMod.MODID + "." + path;
    }

    public ResourceLocation id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public static ShopDiagnosticCode fromOperation(ShopOperationResult result) {
        Objects.requireNonNull(result, "result");
        String reason = result.reason();
        if (ShopOperationResult.REASON_OPENED.equals(reason)) {
            return OPENED;
        }
        if (ShopOperationResult.REASON_ALREADY_OPEN.equals(reason)) {
            return ALREADY_OPEN;
        }
        if (ShopOperationResult.REASON_OPEN_REQUEST_DISABLED.equals(reason)) {
            return OPEN_REQUEST_DISABLED;
        }
        if (ShopOperationResult.REASON_ALREADY_CLOSED.equals(reason)) {
            return ALREADY_CLOSED;
        }
        if (ShopOperationResult.REASON_EMPTY_MENU.equals(reason)) {
            return MENU_EMPTY;
        }
        if (ShopOperationResult.REASON_NO_RESOLVED_LISTINGS.equals(reason)) {
            return NO_RESOLVED_LISTINGS;
        }
        if (ShopOperationResult.REASON_NO_ITEM_CANDIDATES.equals(reason)) {
            return NO_ITEM_CANDIDATES;
        }
        if (ShopOperationResult.REASON_ORDER_CAPACITY_FULL.equals(reason)) {
            return ORDER_CAPACITY_FULL;
        }
        if (result.status() == ShopOperationStatus.CLOSED) {
            return CLOSED;
        }
        if (result.status() == ShopOperationStatus.DENIED_BY_OPENING_RULE) {
            return OPENING_RULE_FAILED;
        }
        if (result.status() == ShopOperationStatus.DENIED_BY_POLICY) {
            return POLICY_CLOSED;
        }
        return NONE;
    }
}

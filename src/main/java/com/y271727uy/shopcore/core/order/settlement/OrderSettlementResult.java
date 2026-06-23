package com.y271727uy.shopcore.core.order.settlement;

import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import com.y271727uy.shopcore.economic.checkout.CheckoutResult;
import com.y271727uy.shopcore.economic.currency.CurrencyOperationResult;
import com.y271727uy.shopcore.economic.tax.Tax;

import java.util.Objects;
import java.util.Optional;

public record OrderSettlementResult(
        OrderSettlementMode mode,
        boolean settled,
        long grossAmount,
        long netAmount,
        long taxAmount,
        double reputationReward,
        Tax.TaxResult taxResult,
        CheckoutResult checkoutResult,
        Optional<CurrencyOperationResult> currencyOperation,
        BusinessSessionStats sessionStats
) {
    public OrderSettlementResult {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(taxResult, "taxResult");
        Objects.requireNonNull(checkoutResult, "checkoutResult");
        currencyOperation = Objects.requireNonNullElse(currencyOperation, Optional.empty());
        Objects.requireNonNull(sessionStats, "sessionStats");
        if (grossAmount < 0L || netAmount < 0L || taxAmount < 0L) {
            throw new IllegalArgumentException("settlement amounts cannot be negative");
        }
        if (!Double.isFinite(reputationReward) || reputationReward < 0.0D) {
            throw new IllegalArgumentException("reputationReward must be a finite non-negative value");
        }
    }
}

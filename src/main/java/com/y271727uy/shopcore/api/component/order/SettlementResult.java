package com.y271727uy.shopcore.api.component.order;

import com.y271727uy.shopcore.api.component.economic.CheckoutResult;
import com.y271727uy.shopcore.api.component.economic.CurrencyOperationResult;
import java.util.Objects;
import java.util.Optional;

/** Result of this settlement attempt, not a promise of a future payment or retry. */
public record SettlementResult(SettlementMode mode, boolean settled, long grossAmount, long netAmount,
        long taxAmount, double reputationReward, CheckoutResult checkoutResult,
        Optional<CurrencyOperationResult> currencyOperation) {
    public SettlementResult {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(checkoutResult, "checkoutResult");
        Objects.requireNonNull(currencyOperation, "currencyOperation");
    }
}

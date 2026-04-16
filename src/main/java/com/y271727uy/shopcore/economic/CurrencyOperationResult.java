package com.y271727uy.shopcore.economic;

import java.util.OptionalDouble;
import java.util.Objects;

/**
 * Result returned by the SDM economy currency bridge.
 *
 * @param success whether the CurrencyHelper call completed successfully
 * @param delta requested currency change; positive for increase, negative for decrease
 * @param balanceAfter balance after the operation, if it can be queried
 * @param message human-readable status message
 */
@SuppressWarnings("unused")
public record CurrencyOperationResult(boolean success, double delta, OptionalDouble balanceAfter, String message) {
    public CurrencyOperationResult {
        balanceAfter = Objects.requireNonNullElse(balanceAfter, OptionalDouble.empty());
        message = message == null ? "" : message;
    }

    public static CurrencyOperationResult success(double delta, OptionalDouble balanceAfter, String message) {
        return new CurrencyOperationResult(true, delta, balanceAfter, message);
    }

    public static CurrencyOperationResult failure(double delta, String message) {
        return new CurrencyOperationResult(false, delta, OptionalDouble.empty(), message);
    }
}




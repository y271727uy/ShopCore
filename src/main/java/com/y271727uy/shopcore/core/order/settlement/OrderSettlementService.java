package com.y271727uy.shopcore.core.order.settlement;

import com.y271727uy.shopcore.api.economic.ShopcoreCheckout;
import com.y271727uy.shopcore.api.economic.ShopcoreCurrency;
import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import com.y271727uy.shopcore.economic.checkout.CheckoutInput;
import com.y271727uy.shopcore.economic.checkout.CheckoutResult;
import com.y271727uy.shopcore.economic.currency.CurrencyOperationResult;
import com.y271727uy.shopcore.economic.tax.Tax;

import java.util.Objects;
import java.util.Optional;

public final class OrderSettlementService {
    private OrderSettlementService() {
    }

    public static OrderSettlementResult settle(OrderSettlementContext context, BusinessSessionStats sessionStats) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(sessionStats, "sessionStats");

        long grossBasis = Math.max(0L, Math.addExact(
                Math.min(context.order().deliveredValue(), context.order().totalValue()),
                context.order().qualityBonusPrice()
        ));
        double payoutMultiplier = context.evaluation().accepted() ? context.evaluation().payoutMultiplier() : 0.0D;
        CheckoutInput checkoutInput = new CheckoutInput(
                grossBasis,
                0.0D,
                context.reputationBase() + context.order().qualityBonusReputation(),
                1,
                payoutMultiplier
        );
        CheckoutResult grossCheckout = ShopcoreCheckout.checkout(checkoutInput, null);

        Tax.TaxResult taxResult = context.mode() == OrderSettlementMode.ACCOUNT_DEPOSIT
                ? Tax.calculate(grossCheckout.totalValue(), context.taxExempt())
                : Tax.calculate(grossCheckout.totalValue(), true);
        long payoutAmount = context.mode() == OrderSettlementMode.ACCOUNT_DEPOSIT
                ? taxResult.netAmount()
                : grossCheckout.totalValue();
        CheckoutInput payoutInput = new CheckoutInput(payoutAmount, 0.0D, grossCheckout.reputationReward(), 1, 1.0D);
        CheckoutResult checkoutResult = context.mode() == OrderSettlementMode.CURRENCY_ITEMS
                ? ShopcoreCheckout.checkout(payoutInput)
                : ShopcoreCheckout.checkout(payoutInput, null);
        Optional<CurrencyOperationResult> currencyOperation = Optional.empty();
        boolean settled = context.evaluation().accepted();

        if (context.mode() == OrderSettlementMode.ACCOUNT_DEPOSIT && settled) {
            CurrencyOperationResult operation = ShopcoreCurrency.increase(context.accountUuid().orElseThrow(), (double) taxResult.netAmount());
            currencyOperation = Optional.of(operation);
            settled = operation.success();
        }

        BusinessSessionStats updatedStats = settled
                ? sessionStats.recordOrderCompleted(
                taxResult.grossAmount(),
                taxResult.netAmount(),
                taxResult.taxAmount(),
                context.order().totalDeliveredCount()
        )
                : sessionStats;

        return new OrderSettlementResult(
                context.mode(),
                settled,
                taxResult.grossAmount(),
                taxResult.netAmount(),
                taxResult.taxAmount(),
                checkoutResult.reputationReward(),
                taxResult,
                checkoutResult,
                currencyOperation,
                updatedStats
        );
    }
}

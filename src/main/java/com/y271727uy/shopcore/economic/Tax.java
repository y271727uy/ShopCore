package com.y271727uy.shopcore.economic;

/**
 * Pure tax calculator for ShopCore sale settlements.
 */
@SuppressWarnings("unused")
public final class Tax {
    private static final long FIRST_TAX_THRESHOLD = 1_000L;
    private static final long SECOND_TAX_THRESHOLD = 5_000L;
    private static final int FIRST_TAX_RATE = 5;
    private static final int SECOND_TAX_RATE = 15;

    private Tax() {
    }

    public static TaxResult calculate(long grossAmount) {
        return calculate(grossAmount, false);
    }

    public static TaxResult calculate(long grossAmount, boolean exempt) {
        long safeGross = Math.max(0L, grossAmount);
        if (safeGross == 0L) {
            return new TaxResult(0L, 0, 0L, 0L, exempt);
        }

        int rate = rateFor(safeGross, exempt);
        long taxAmount = taxAmount(safeGross, rate);
        long netAmount = safeGross - taxAmount;
        return new TaxResult(safeGross, rate, taxAmount, netAmount, exempt);
    }

    public static int rateFor(long grossAmount) {
        return rateFor(grossAmount, false);
    }

    public static int rateFor(long grossAmount, boolean exempt) {
        if (exempt) {
            return 0;
        }

        long safeGross = Math.max(0L, grossAmount);
        if (safeGross > SECOND_TAX_THRESHOLD) {
            return SECOND_TAX_RATE;
        }
        if (safeGross > FIRST_TAX_THRESHOLD) {
            return FIRST_TAX_RATE;
        }
        return 0;
    }

    public static long taxAmount(long grossAmount, boolean exempt) {
        return taxAmount(Math.max(0L, grossAmount), rateFor(grossAmount, exempt));
    }

    public static long taxAmount(long grossAmount, int taxRatePercent) {
        long safeGross = Math.max(0L, grossAmount);
        if (safeGross == 0L || taxRatePercent <= 0) {
            return 0L;
        }

        return (safeGross * taxRatePercent) / 100L;
    }

    public record TaxResult(long grossAmount, int taxRatePercent, long taxAmount, long netAmount, boolean exempt) {
    }
}




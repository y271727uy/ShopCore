package com.y271727uy.shopcore.api.economic;

import com.y271727uy.shopcore.economic.CurrencyOperationResult;
import com.y271727uy.shopcore.integration.sdm_integration.SdmCurrencyHelperBridge;
import net.minecraft.world.entity.player.Player;

import java.util.OptionalDouble;

/**
 * Public currency API for interacting with SDM Economy.
 */
@SuppressWarnings("unused")
public final class ShopcoreCurrency {
    private ShopcoreCurrency() {
    }

    public static boolean isAvailable() {
        return SdmCurrencyHelperBridge.isAvailable();
    }

    public static CurrencyOperationResult increase(Player player, double amount) {
        return SdmCurrencyHelperBridge.increase(player, amount);
    }

    public static CurrencyOperationResult decrease(Player player, double amount) {
        return SdmCurrencyHelperBridge.decrease(player, amount);
    }

    public static CurrencyOperationResult adjust(Player player, double delta) {
        return SdmCurrencyHelperBridge.adjust(player, delta);
    }

    public static OptionalDouble balance(Player player) {
        return SdmCurrencyHelperBridge.queryBalance(player);
    }
}



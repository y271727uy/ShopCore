package com.y271727uy.shopcore.api.shop;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.UUID;

/** Public, immutable view of a ShopCore shop infrastructure instance. */
public record ShopSnapshot(
        UUID id,
        BlockPos position,
        String shopTier,
        String marketTier,
        String operatingState,
        double reputation,
        Optional<UUID> settlementAccount,
        boolean settlementTaxExempt,
        int activeOrderCount
) {
    public ShopSnapshot {
        position = position.immutable();
        settlementAccount = settlementAccount == null ? Optional.empty() : settlementAccount;
    }

    public boolean hasSettlementAccount() {
        return settlementAccount.isPresent();
    }

    public boolean isOpen() {
        return "OPEN".equals(operatingState);
    }
}

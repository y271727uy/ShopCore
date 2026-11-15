package com.y271727uy.shopcore.core.shop.instance;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyKey;
import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import net.minecraft.core.BlockPos;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Runtime/state payload for one shop block or persisted shop record.
 * Ownership and settlement are external concerns and should not be used as the shop primary key.
 */
public record ShopInstance(
        ShopId shopId,
        BlockPos shopPos,
        ShopTier shopTier,
        MarketTier marketTier,
        OperatingPolicyKey operatingPolicy,
        ShopOperatingState operatingState,
        BusinessSessionStats currentSession,
        double reputation,
        Optional<UUID> settlementAccountUuid,
        boolean settlementTaxExempt
) {
    public ShopInstance {
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(shopPos, "shopPos");
        Objects.requireNonNull(shopTier, "shopTier");
        Objects.requireNonNull(marketTier, "marketTier");
        Objects.requireNonNull(operatingPolicy, "operatingPolicy");
        Objects.requireNonNull(operatingState, "operatingState");
        Objects.requireNonNull(currentSession, "currentSession");
        settlementAccountUuid = Objects.requireNonNullElse(settlementAccountUuid, Optional.empty());
        if (!Double.isFinite(reputation) || reputation < 0.0) {
            throw new IllegalArgumentException("reputation must be finite and non-negative");
        }
    }

    public static ShopInstance create(BlockPos shopPos) {
        return new ShopInstance(
                ShopId.random(),
                shopPos,
                ShopTier.TIER_1,
                MarketTier.TIER_1,
                OperatingPolicyKey.MANUAL,
                ShopOperatingState.CLOSED,
                BusinessSessionStats.empty(),
                0.0,
                Optional.empty(),
                false
        );
    }

    public boolean isOpen() {
        return operatingState == ShopOperatingState.OPEN && currentSession.isOpen();
    }

    public boolean canAcceptOrders() {
        return isOpen();
    }

    public ShopInstance open(long gameTime) {
        return new ShopInstance(
                shopId,
                shopPos,
                shopTier,
                marketTier,
                operatingPolicy,
                ShopOperatingState.OPEN,
                BusinessSessionStats.open(gameTime),
                reputation,
                settlementAccountUuid,
                settlementTaxExempt
        );
    }

    public ShopInstance close(long gameTime) {
        return new ShopInstance(
                shopId,
                shopPos,
                shopTier,
                marketTier,
                operatingPolicy,
                ShopOperatingState.CLOSED,
                currentSession.close(gameTime),
                reputation,
                settlementAccountUuid,
                settlementTaxExempt
        );
    }

    public ShopInstance withShopTier(ShopTier shopTier) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession, reputation, settlementAccountUuid, settlementTaxExempt);
    }

    public ShopInstance withMarketTier(MarketTier marketTier) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession, reputation, settlementAccountUuid, settlementTaxExempt);
    }

    public ShopInstance withOperatingPolicy(OperatingPolicyKey operatingPolicy) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession, reputation, settlementAccountUuid, settlementTaxExempt);
    }

    public ShopInstance withCurrentSession(BusinessSessionStats currentSession) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession, reputation, settlementAccountUuid, settlementTaxExempt);
    }

    public ShopInstance addReputation(double amount) {
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("amount must be finite");
        }
        return new ShopInstance(
                shopId,
                shopPos,
                shopTier,
                marketTier,
                operatingPolicy,
                operatingState,
                currentSession,
                Math.max(0.0, reputation + amount),
                settlementAccountUuid,
                settlementTaxExempt
        );
    }

    public boolean hasSettlementAccount() {
        return settlementAccountUuid.isPresent();
    }

    public boolean isSettlementAccount(UUID accountUuid) {
        return settlementAccountUuid.filter(accountUuid::equals).isPresent();
    }

    public ShopInstance withSettlementAccount(UUID accountUuid, boolean taxExempt) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState,
                currentSession, reputation, Optional.of(Objects.requireNonNull(accountUuid, "accountUuid")), taxExempt);
    }

    public ShopInstance withoutSettlementAccount() {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState,
                currentSession, reputation, Optional.empty(), false);
    }
}

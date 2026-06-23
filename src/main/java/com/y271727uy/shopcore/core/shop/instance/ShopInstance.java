package com.y271727uy.shopcore.core.shop.instance;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyKey;
import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import net.minecraft.core.BlockPos;

import java.util.Objects;

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
        BusinessSessionStats currentSession
) {
    public ShopInstance {
        Objects.requireNonNull(shopId, "shopId");
        Objects.requireNonNull(shopPos, "shopPos");
        Objects.requireNonNull(shopTier, "shopTier");
        Objects.requireNonNull(marketTier, "marketTier");
        Objects.requireNonNull(operatingPolicy, "operatingPolicy");
        Objects.requireNonNull(operatingState, "operatingState");
        Objects.requireNonNull(currentSession, "currentSession");
    }

    public static ShopInstance create(BlockPos shopPos) {
        return new ShopInstance(
                ShopId.random(),
                shopPos,
                ShopTier.TIER_1,
                MarketTier.TIER_1,
                OperatingPolicyKey.MANUAL,
                ShopOperatingState.CLOSED,
                BusinessSessionStats.empty()
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
                BusinessSessionStats.open(gameTime)
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
                currentSession.close(gameTime)
        );
    }

    public ShopInstance withShopTier(ShopTier shopTier) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession);
    }

    public ShopInstance withMarketTier(MarketTier marketTier) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession);
    }

    public ShopInstance withOperatingPolicy(OperatingPolicyKey operatingPolicy) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession);
    }

    public ShopInstance withCurrentSession(BusinessSessionStats currentSession) {
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession);
    }
}

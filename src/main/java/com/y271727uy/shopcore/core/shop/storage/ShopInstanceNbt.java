package com.y271727uy.shopcore.core.shop.storage;

import com.y271727uy.shopcore.core.market.tier.MarketTier;
import com.y271727uy.shopcore.core.shop.instance.ShopId;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.instance.ShopOperatingState;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyKey;
import com.y271727uy.shopcore.core.shop.session.BusinessSessionStats;
import com.y271727uy.shopcore.core.shop.tier.ShopTier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class ShopInstanceNbt {
    public static final String SHOP = "Shop";

    private static final String SHOP_ID = "ShopId";
    private static final String SHOP_POS = "ShopPos";
    private static final String SHOP_TIER = "ShopTier";
    private static final String MARKET_TIER = "MarketTier";
    private static final String OPERATING_POLICY = "OperatingPolicy";
    private static final String OPERATING_STATE = "OperatingState";
    private static final String CURRENT_SESSION = "CurrentSession";

    private ShopInstanceNbt() {
    }

    public static CompoundTag save(ShopInstance shop) {
        Objects.requireNonNull(shop, "shop");
        CompoundTag tag = new CompoundTag();
        ShopIdNbt.put(tag, SHOP_ID, shop.shopId());
        tag.put(SHOP_POS, NbtUtils.writeBlockPos(shop.shopPos()));
        tag.putString(SHOP_TIER, shop.shopTier().name());
        tag.putString(MARKET_TIER, shop.marketTier().name());
        tag.putString(OPERATING_POLICY, shop.operatingPolicy().id().toString());
        tag.putString(OPERATING_STATE, shop.operatingState().name());
        tag.put(CURRENT_SESSION, BusinessSessionStatsNbt.save(shop.currentSession()));
        return tag;
    }

    public static ShopInstance load(CompoundTag tag, BlockPos fallbackPos) {
        Objects.requireNonNull(tag, "tag");
        Objects.requireNonNull(fallbackPos, "fallbackPos");
        ShopId fallbackId = ShopId.random();
        ShopId shopId = ShopIdNbt.get(tag, SHOP_ID, fallbackId);
        BlockPos shopPos = tag.contains(SHOP_POS) ? NbtUtils.readBlockPos(tag.getCompound(SHOP_POS)) : fallbackPos;
        ShopTier shopTier = enumValue(tag.getString(SHOP_TIER), ShopTier.TIER_1);
        MarketTier marketTier = enumValue(tag.getString(MARKET_TIER), MarketTier.TIER_1);
        OperatingPolicyKey operatingPolicy = operatingPolicy(tag.getString(OPERATING_POLICY));
        ShopOperatingState operatingState = enumValue(tag.getString(OPERATING_STATE), ShopOperatingState.CLOSED);
        BusinessSessionStats currentSession = tag.contains(CURRENT_SESSION)
                ? BusinessSessionStatsNbt.load(tag.getCompound(CURRENT_SESSION))
                : BusinessSessionStats.empty();
        return new ShopInstance(shopId, shopPos, shopTier, marketTier, operatingPolicy, operatingState, currentSession);
    }

    private static OperatingPolicyKey operatingPolicy(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            return OperatingPolicyKey.MANUAL;
        }
        return OperatingPolicyKey.of(id);
    }

    private static <T extends Enum<T>> T enumValue(String value, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(fallback.getDeclaringClass(), value);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}

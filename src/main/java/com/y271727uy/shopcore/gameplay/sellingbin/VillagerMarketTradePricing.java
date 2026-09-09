package com.y271727uy.shopcore.gameplay.sellingbin;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** Applies the selling-bin market to offers when a villager creates or restocks trades. */
public final class VillagerMarketTradePricing {
    private static final String BASELINE_TAG = ShopcoreMod.MODID + ":market_trade_baselines";
    private static final String BUY_A_TAG = "BuyA";
    private static final String SELL_TAG = "Sell";
    private static final double PRICE_POINT_FACTOR = 0.10D;
    private static final double MIN_PRICE_FACTOR = 0.25D;

    private VillagerMarketTradePricing() {
    }

    public static void refresh(Villager villager) {
        if (!(villager.level() instanceof ServerLevel level)) {
            return;
        }

        MerchantOffers offers = villager.getOffers();
        ListTag baselines = villager.getPersistentData().getList(BASELINE_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < offers.size(); index++) {
            MerchantOffer offer = offers.get(index);
            CompoundTag baseline = baselineAt(baselines, index, offer);
            applyBaseline(offer, baseline);
            applyMarketPrice(level, offer);
        }
        villager.getPersistentData().put(BASELINE_TAG, baselines);
    }

    private static CompoundTag baselineAt(ListTag baselines, int index, MerchantOffer offer) {
        while (baselines.size() <= index) {
            CompoundTag baseline = new CompoundTag();
            baseline.put(BUY_A_TAG, offer.getBaseCostA().copy().save(new CompoundTag()));
            baseline.put(SELL_TAG, offer.getResult().copy().save(new CompoundTag()));
            baselines.add(baseline);
        }
        return baselines.getCompound(index);
    }

    private static void applyBaseline(MerchantOffer offer, CompoundTag baseline) {
        if (baseline.contains(BUY_A_TAG, Tag.TAG_COMPOUND)) {
            offer.getBaseCostA().setCount(ItemStack.of(baseline.getCompound(BUY_A_TAG)).getCount());
        }
        if (baseline.contains(SELL_TAG, Tag.TAG_COMPOUND)) {
            offer.getResult().setCount(ItemStack.of(baseline.getCompound(SELL_TAG)).getCount());
        }
    }

    private static void applyMarketPrice(ServerLevel level, MerchantOffer offer) {
        ItemStack buyA = offer.getBaseCostA();
        ItemStack result = offer.getResult();
        if (buyA.is(Items.EMERALD) && !result.is(Items.EMERALD)) {
            buyA.setCount(adjustCount(buyA.getCount(), priceBonus(level, result)));
        } else if (result.is(Items.EMERALD) && !buyA.is(Items.EMERALD)) {
            result.setCount(adjustCount(result.getCount(), priceBonus(level, buyA)));
        }
    }

    private static int priceBonus(ServerLevel level, ItemStack stack) {
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return SellingBinGroupManager.getPriceBonus(level, itemId);
    }

    private static int adjustCount(int baseCount, int marketBonus) {
        double factor = Math.max(MIN_PRICE_FACTOR, 1.0D + marketBonus * PRICE_POINT_FACTOR);
        return Mth.clamp((int) Math.round(baseCount * factor), 1, 64);
    }
}

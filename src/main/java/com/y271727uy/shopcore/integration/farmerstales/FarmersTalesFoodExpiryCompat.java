package com.y271727uy.shopcore.integration.farmerstales;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Reads Farmer's Tales food-expiry NBT without making the mod a hard runtime dependency. */
public final class FarmersTalesFoodExpiryCompat {
    private static final String EXPIRY_TAG = "FarmerstalesFoodExpiry";
    private static final String DURATION_TAG = "FarmerstalesFoodDuration";
    private static final String ROTTEN_TAG = "FarmerstalesFoodRotten";

    private FarmersTalesFoodExpiryCompat() {
    }

    public static Freshness getFreshness(ItemStack stack, long gameTime) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return Freshness.UNTRACKED;
        }
        if (tag.getBoolean(ROTTEN_TAG)) {
            return Freshness.ROTTEN;
        }
        if (!tag.contains(EXPIRY_TAG, CompoundTag.TAG_LONG) || !tag.contains(DURATION_TAG, CompoundTag.TAG_LONG)) {
            return Freshness.UNTRACKED;
        }

        long remainingTicks = tag.getLong(EXPIRY_TAG) - gameTime;
        long durationTicks = tag.getLong(DURATION_TAG);
        if (remainingTicks <= 0L) {
            return Freshness.ROTTEN;
        }
        if (durationTicks == Long.MAX_VALUE || (double) remainingTicks / Math.max(1L, durationTicks) >= 2.0D / 3.0D) {
            return Freshness.FRESH;
        }
        return Freshness.AGING;
    }

    public static CustomerDeliveryBonus getCustomerDeliveryBonus(ItemStack stack, long gameTime) {
        return switch (getFreshness(stack, gameTime)) {
            case FRESH -> new CustomerDeliveryBonus(1, 5.0D, false);
            case ROTTEN -> new CustomerDeliveryBonus(-5, -10.0D, true);
            case AGING, UNTRACKED -> CustomerDeliveryBonus.NONE;
        };
    }

    public enum Freshness {
        FRESH,
        AGING,
        ROTTEN,
        UNTRACKED
    }

    public record CustomerDeliveryBonus(int revenueAdjustment, double reputationAdjustment, boolean rotten) {
        public static final CustomerDeliveryBonus NONE = new CustomerDeliveryBonus(0, 0.0D, false);
    }
}

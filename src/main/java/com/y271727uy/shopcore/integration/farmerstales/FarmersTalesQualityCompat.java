package com.y271727uy.shopcore.integration.farmerstales;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** Reads the stable Farmer's Tales quality NBT contract without requiring that mod at runtime. */
public final class FarmersTalesQualityCompat {
    private static final String LEGACY_QUALITY_KEY = "quality";
    private static final String[] QUALITY_KEYS = {"", "quality1", "quality2", "quality3"};
    private static final int[] MIN_PRICE_BONUSES = {0, 1, 2, 3};
    private static final int[] MAX_PRICE_BONUSES = {0, 1, 2, 4};

    private FarmersTalesQualityCompat() {
    }

    public static int getMinPriceBonus(ItemStack stack) {
        return MIN_PRICE_BONUSES[getQualityId(stack)];
    }

    public static int getMaxPriceBonus(ItemStack stack) {
        return MAX_PRICE_BONUSES[getQualityId(stack)];
    }

    public static int rollPriceBonus(ItemStack stack, RandomSource random) {
        int qualityId = getQualityId(stack);
        int min = MIN_PRICE_BONUSES[qualityId];
        int max = MAX_PRICE_BONUSES[qualityId];
        return max <= min ? min : min + random.nextInt(max - min + 1);
    }

    /** Customer-delivery rewards for Farmer's Tales quality crops, per delivered item. */
    public static CustomerDeliveryBonus getCustomerDeliveryBonus(ItemStack stack) {
        return switch (getQualityId(stack)) {
            case 1 -> new CustomerDeliveryBonus(1, 6.0D);
            case 2 -> new CustomerDeliveryBonus(2, 8.0D);
            case 3 -> new CustomerDeliveryBonus(3, 10.0D);
            default -> CustomerDeliveryBonus.NONE;
        };
    }

    private static int getQualityId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }

        for (int qualityId = 3; qualityId >= 1; qualityId--) {
            String key = QUALITY_KEYS[qualityId];
            if (tag.contains(key) && (tag.getInt(key) > 0 || tag.getBoolean(key))) {
                return qualityId;
            }
        }

        if (!tag.contains(LEGACY_QUALITY_KEY)) {
            return 0;
        }
        int legacyQualityId = tag.getInt(LEGACY_QUALITY_KEY);
        return legacyQualityId >= 1 && legacyQualityId <= 3 ? legacyQualityId : 0;
    }

    public record CustomerDeliveryBonus(int extraPrice, double extraReputation) {
        public static final CustomerDeliveryBonus NONE = new CustomerDeliveryBonus(0, 0.0D);
    }
}

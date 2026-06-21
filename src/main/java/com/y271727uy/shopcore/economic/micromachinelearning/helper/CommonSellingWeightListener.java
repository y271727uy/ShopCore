package com.y271727uy.shopcore.economic.micromachinelearning.helper;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.economic.micromachinelearning.weight.ItemTagWeightAmountPolicy;
import com.y271727uy.shopcore.event.CommonSellingEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Bridges the common selling event into the player-scoped weight and price pressure helper.
 */
@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CommonSellingWeightListener {
    private static final ItemTagWeightAmountPolicy AMOUNT_POLICY = ItemTagWeightAmountPolicy.DEFAULT;
    private static final MicroPriceAdjustmentHelper<UUID, ResourceLocation> HELPER = new MicroPriceAdjustmentHelper<>();

    private CommonSellingWeightListener() {
    }

    @SubscribeEvent
    public static void onCommonSelling(CommonSellingEvents event) {
        if (event.getPlayer().level().isClientSide) {
            return;
        }

        double weightAmount = AMOUNT_POLICY.unitAmountOf(event.getSoldStack()) * event.getQuantity();
        PriceAdjustmentResult result = HELPER.adjustSellPrice(new PriceAdjustmentRequest<>(
                event.getPlayer().getUUID(),
                event.getItemKey(),
                event.getBaseSellPrice(),
                weightAmount,
                event.getNowTick()
        ));
        event.applyPriceAdjustment(result);
    }

    public static MicroPriceAdjustmentHelper<UUID, ResourceLocation> helper() {
        return HELPER;
    }
}

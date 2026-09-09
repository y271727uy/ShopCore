package com.y271727uy.shopcore.mixin;

import com.y271727uy.shopcore.gameplay.sellingbin.VillagerMarketTradePricing;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
abstract class VillagerMarketPriceMixin {
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void shopcore$applyMarketPricesToNewTrades(CallbackInfo callback) {
        VillagerMarketTradePricing.refresh((Villager) (Object) this);
    }

    @Inject(method = "restock", at = @At("HEAD"))
    private void shopcore$applyMarketPricesOnRestock(CallbackInfo callback) {
        VillagerMarketTradePricing.refresh((Villager) (Object) this);
    }
}

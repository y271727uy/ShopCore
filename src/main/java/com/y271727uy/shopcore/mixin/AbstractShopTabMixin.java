package com.y271727uy.shopcore.mixin;

import com.y271727uy.shopcore.integration.jei.sdmshop.event.ShopDataLoadedEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.MinecraftForge;
import net.sixik.sdmshoprework.api.shop.AbstractShopTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractShopTab.class, remap = false)
public abstract class AbstractShopTabMixin {
    @Inject(method = "createShopEntry(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"), remap = false)
    private void shopcore_refreshShopEntries(CompoundTag nbt, CallbackInfo ci) {
        Object shopTab = this;
        com.y271727uy.shopcore.integration.jei.sdmshop.SdmShopDataBridge
                .createShopEntrySnapshot(shopTab, nbt)
                .ifPresent(entry -> MinecraftForge.EVENT_BUS.post(new ShopDataLoadedEvent(shopTab, entry)));
    }
}







package com.y271727uy.shopcore.integration.jade;

import com.y271727uy.shopcore.block.SellingBinBlock;
import com.y271727uy.shopcore.text.TestShopBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ShopcoreJadePlugin implements IWailaPlugin {
    public void register(IWailaCommonRegistration registration) {
    }

    public void registerClient(IWailaClientRegistration registration) {
        // SellingBin provider
        registration.registerBlockComponent(SellingBinTooltipProvider.INSTANCE, SellingBinBlock.class);

        // Shop reputation provider - register for specific shop block types
        registration.registerBlockComponent(ShopReputationTooltipProvider.INSTANCE, TestShopBlock.class);
    }
}

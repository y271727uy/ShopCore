package com.list.integration.jade;

import com.list.block.FishPondCoreBlock;
import com.list.block.TapperBlock;
import com.list.block.entity.FishPondCoreBlockEntity;
import com.list.block.entity.TapperBlockEntity;
import com.list.gameplay.fish_group.entity.OceanFishPoolEntity;
import com.list.gameplay.fish_group.entity.RiverFishPoolEntity;
import com.list.integration.jade.provider.FishPoolEntityProvider;
import com.list.integration.jade.provider.FishPondProvider;
import com.list.integration.jade.provider.TapperProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ListJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FishPondProvider.INSTANCE, FishPondCoreBlockEntity.class);
        registration.registerBlockDataProvider(TapperProvider.INSTANCE, TapperBlockEntity.class);
        registration.registerEntityDataProvider(FishPoolEntityProvider.INSTANCE, RiverFishPoolEntity.class);
        registration.registerEntityDataProvider(FishPoolEntityProvider.INSTANCE, OceanFishPoolEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FishPondProvider.INSTANCE, FishPondCoreBlock.class);
        registration.registerBlockComponent(TapperProvider.INSTANCE, TapperBlock.class);
        registration.registerEntityComponent(FishPoolEntityProvider.INSTANCE, RiverFishPoolEntity.class);
        registration.registerEntityComponent(FishPoolEntityProvider.INSTANCE, OceanFishPoolEntity.class);
    }
}

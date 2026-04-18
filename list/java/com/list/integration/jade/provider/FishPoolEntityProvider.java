package com.list.integration.jade.provider;

import com.list.ListMod;
import com.list.gameplay.fish_group.entity.AbstractFishPoolEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FishPoolEntityProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) {
            return;
        }

        tooltip.add(Component.translatable(
                "tooltip.list.fish_pool.fish_king",
                Component.translatable(data.getString("fishKingKey"))
        ));

        tooltip.add(Component.translatable(
                "tooltip.list.fish_pool.weather",
                Component.translatable(data.getString("weatherKey"))
        ));

        tooltip.add(Component.translatable(
                "tooltip.list.fish_pool.time",
                Component.translatable(data.getString("timeKey"))
        ));

        tooltip.add(Component.translatable(
                "tooltip.list.fish_pool.state",
                Component.translatable(data.getString("stateKey"))
        ));
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof AbstractFishPoolEntity fishPoolEntity)) {
            return;
        }
        if (!(accessor.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        fishPoolEntity.appendJadeServerData(tag, serverLevel);
    }

    @Override
    public ResourceLocation getUid() {
        return ListMod.rl("fish_pool");
    }
}



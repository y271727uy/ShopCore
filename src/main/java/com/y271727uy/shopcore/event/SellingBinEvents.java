package com.y271727uy.shopcore.event;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.gameplay.sellingbin.SellingBinGroupManager;
import com.y271727uy.shopcore.network.ModMessages;
import com.y271727uy.shopcore.network.SellingBinPriceSyncS2CPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SellingBinEvents {
    private SellingBinEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!serverLevel.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        if (SellingBinGroupManager.refreshForElapsedDays(serverLevel)) {
            syncAllPlayers(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncPlayer(serverPlayer);
        }
    }

    private static void syncAllPlayers(ServerLevel level) {
        ModMessages.get().send(PacketDistributor.ALL.noArg(), new SellingBinPriceSyncS2CPacket(
                SellingBinGroupManager.snapshotPriceBonuses(level)
        ));
    }

    private static void syncPlayer(ServerPlayer player) {
        ModMessages.get().send(PacketDistributor.PLAYER.with(() -> player), new SellingBinPriceSyncS2CPacket(
                SellingBinGroupManager.snapshotPriceBonuses(player.serverLevel())
        ));
    }
}


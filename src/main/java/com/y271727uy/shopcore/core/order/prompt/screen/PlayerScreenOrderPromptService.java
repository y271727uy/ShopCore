package com.y271727uy.shopcore.core.order.prompt.screen;

import com.y271727uy.shopcore.core.order.prompt.OrderPrompt;
import com.y271727uy.shopcore.network.ModMessages;
import com.y271727uy.shopcore.network.OrderPromptScreenS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;

public final class PlayerScreenOrderPromptService {
    private PlayerScreenOrderPromptService() {
    }

    public static void show(ServerPlayer player, OrderPrompt prompt) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(prompt, "prompt");
        ModMessages.get().send(PacketDistributor.PLAYER.with(() -> player), OrderPromptScreenS2CPacket.show(prompt));
    }

    public static void clear(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        ModMessages.get().send(PacketDistributor.PLAYER.with(() -> player), OrderPromptScreenS2CPacket.clear());
    }
}

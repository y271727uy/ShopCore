package com.y271727uy.shopcore.network;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    @SuppressWarnings("removal")
    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SellingBinPriceSyncS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SellingBinPriceSyncS2CPacket::encode)
                .decoder(SellingBinPriceSyncS2CPacket::decode)
                .consumerMainThread(SellingBinPriceSyncS2CPacket::handle)
                .add();
    }

    public static SimpleChannel get() {
        return INSTANCE;
    }
}
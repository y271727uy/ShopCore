package com.y271727uy.shopcore.network;

import com.y271727uy.shopcore.client.sellingbin.SellingBinClientPriceCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SellingBinPriceSyncS2CPacket {
    private final Map<ResourceLocation, Integer> totalPriceBonusByRecipe;
    private final Map<ResourceLocation, Integer> seasonalPriceBonusByRecipe;

    public SellingBinPriceSyncS2CPacket(Map<ResourceLocation, Integer> totalPriceBonusByRecipe, Map<ResourceLocation, Integer> seasonalPriceBonusByRecipe) {
        this.totalPriceBonusByRecipe = Map.copyOf(totalPriceBonusByRecipe);
        this.seasonalPriceBonusByRecipe = Map.copyOf(seasonalPriceBonusByRecipe);
    }

    public static void encode(SellingBinPriceSyncS2CPacket packet, FriendlyByteBuf buf) {
        writeBonusMap(buf, packet.totalPriceBonusByRecipe);
        writeBonusMap(buf, packet.seasonalPriceBonusByRecipe);
    }

    public static SellingBinPriceSyncS2CPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, Integer> totalPriceBonusByRecipe = readBonusMap(buf);
        Map<ResourceLocation, Integer> seasonalPriceBonusByRecipe = readBonusMap(buf);
        return new SellingBinPriceSyncS2CPacket(totalPriceBonusByRecipe, seasonalPriceBonusByRecipe);
    }

    public static void handle(SellingBinPriceSyncS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                SellingBinClientPriceCache.applySnapshot(packet.totalPriceBonusByRecipe, packet.seasonalPriceBonusByRecipe)
        ));
        context.setPacketHandled(true);
    }

    private static void writeBonusMap(FriendlyByteBuf buf, Map<ResourceLocation, Integer> bonusByRecipe) {
        buf.writeVarInt(bonusByRecipe.size());
        bonusByRecipe.forEach((recipeId, bonus) -> {
            buf.writeResourceLocation(recipeId);
            buf.writeVarInt(bonus);
        });
    }

    private static Map<ResourceLocation, Integer> readBonusMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Integer> bonusByRecipe = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            bonusByRecipe.put(buf.readResourceLocation(), buf.readVarInt());
        }
        return bonusByRecipe;
    }
}

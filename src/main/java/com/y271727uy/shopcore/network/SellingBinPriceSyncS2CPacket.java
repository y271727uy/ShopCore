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
    private final Map<ResourceLocation, Integer> priceBonusByRecipe;

    public SellingBinPriceSyncS2CPacket(Map<ResourceLocation, Integer> priceBonusByRecipe) {
        this.priceBonusByRecipe = Map.copyOf(priceBonusByRecipe);
    }

    public static void encode(SellingBinPriceSyncS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.priceBonusByRecipe.size());
        packet.priceBonusByRecipe.forEach((recipeId, bonus) -> {
            buf.writeResourceLocation(recipeId);
            buf.writeVarInt(bonus);
        });
    }

    public static SellingBinPriceSyncS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<ResourceLocation, Integer> priceBonusByRecipe = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            priceBonusByRecipe.put(buf.readResourceLocation(), buf.readVarInt());
        }
        return new SellingBinPriceSyncS2CPacket(priceBonusByRecipe);
    }

    public static void handle(SellingBinPriceSyncS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                SellingBinClientPriceCache.applySnapshot(packet.priceBonusByRecipe)
        ));
        context.setPacketHandled(true);
    }
}

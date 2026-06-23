package com.y271727uy.shopcore.network;

import com.y271727uy.shopcore.client.order.OrderPromptClientState;
import com.y271727uy.shopcore.core.order.prompt.OrderPrompt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class OrderPromptScreenS2CPacket {
    private final List<ItemStack> displayStacks;

    private OrderPromptScreenS2CPacket(List<ItemStack> displayStacks) {
        this.displayStacks = List.copyOf(displayStacks);
    }

    public static OrderPromptScreenS2CPacket show(OrderPrompt prompt) {
        return new OrderPromptScreenS2CPacket(prompt.lines().stream()
                .map(line -> line.displayStack().copyWithCount(line.count()))
                .toList());
    }

    public static OrderPromptScreenS2CPacket clear() {
        return new OrderPromptScreenS2CPacket(List.of());
    }

    public static void encode(OrderPromptScreenS2CPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.displayStacks.size());
        for (ItemStack stack : packet.displayStacks) {
            buf.writeItem(stack);
        }
    }

    public static OrderPromptScreenS2CPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(buf.readItem());
        }
        return new OrderPromptScreenS2CPacket(stacks);
    }

    public static void handle(OrderPromptScreenS2CPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (packet.displayStacks.isEmpty()) {
                OrderPromptClientState.clear();
            } else {
                OrderPromptClientState.show(packet.displayStacks);
            }
        }));
        context.setPacketHandled(true);
    }
}

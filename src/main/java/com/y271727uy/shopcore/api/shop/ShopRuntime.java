package com.y271727uy.shopcore.api.shop;

import net.minecraft.nbt.CompoundTag;
import com.y271727uy.shopcore.api.component.order.DeliveryResult;
import com.y271727uy.shopcore.api.component.order.OrderSnapshot;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** One shop's runtime. Mutations run on the server thread; NBT sync may be applied on the client. */
public interface ShopRuntime {
    void tick(ServerLevel level);
    boolean toggleOpenRequested();
    void removed(ServerLevel level);
    ShopStatus status();
    Optional<ShopSnapshot> snapshot();
    /** Orders currently retained in the book, including inactive orders; not a permanent history. */
    List<OrderSnapshot> orders();
    default List<OrderSnapshot> activeOrders() { return orders().stream().filter(OrderSnapshot::active).toList(); }
    default Optional<OrderSnapshot> findOrder(UUID orderId) {
        java.util.Objects.requireNonNull(orderId, "orderId");
        return orders().stream().filter(order -> order.orderId().equals(orderId)).findFirst();
    }
    DeliveryResult deliver(ServerPlayer player, Entity consumer, ItemStack input);
    boolean ownsConsumer(UUID consumerId);
    void consumerRemoved(UUID consumerId);
    ShopAccountBindingResult bindSettlementAccount(UUID accountUuid, boolean taxExempt);
    ShopAccountBindingResult unbindSettlementAccount(UUID accountUuid);
    void load(CompoundTag tag);
    void save(CompoundTag tag);
    CompoundTag updateTag();
    void handleUpdateTag(CompoundTag tag);
}

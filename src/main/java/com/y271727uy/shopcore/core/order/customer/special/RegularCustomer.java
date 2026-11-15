package com.y271727uy.shopcore.core.order.customer.special;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

/**
 * Example: Regular Customer - Always orders from specific menu categories
 *
 * Characteristics:
 * - Prefers specific menu IDs (e.g., "burger", "fries")
 * - Only spawns if menu contains their favorite categories
 * - Friendly dialogue
 * - Normal price and reputation
 */
public class RegularCustomer implements SpecialCustomer {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        ShopcoreMod.MODID,
        "regular"
    );

    private final List<String> favoriteMenuIds;
    private final String customerName;

    public RegularCustomer(String customerName, List<String> favoriteMenuIds) {
        this.customerName = customerName;
        this.favoriteMenuIds = List.copyOf(favoriteMenuIds);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Component displayName() {
        return Component.literal(customerName);
    }

    @Override
    public boolean canSpawn(ShopInstance shop, ShopMenuSnapshot menu) {
        // Only spawns if all favorite menu IDs are available
        return favoriteMenuIds.stream()
            .allMatch(menu::hasMenuId);
    }

    @Override
    public Optional<Component> onOrderRecorded(
            ShopInstance shop,
            ShopOrder order,
            ServerPlayer player,
            LivingEntity entity
    ) {
        return Optional.of(Component.translatable(
            "customer.shopcore.regular.order_recorded",
            customerName
        ));
    }

    @Override
    public Optional<Component> onOrderCompleted(
            ShopInstance shop,
            ShopOrder order,
            ServerPlayer player,
            LivingEntity entity,
            long finalPrice,
            double finalReputation
    ) {
        return Optional.of(Component.translatable(
            "customer.shopcore.regular.order_completed",
            customerName
        ));
    }
}

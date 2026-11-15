package com.y271727uy.shopcore.core.order.customer.special;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

/**
 * Example: Oil Tycoon - Rich customer who pays double and tips gold
 *
 * Characteristics:
 * - Pays 2x price
 * - Gives 1.5x reputation
 * - Tips gold ingot when order is recorded
 * - Only spawns if shop reputation >= 50 (Skilled tier)
 * - Higher spawn chance with higher reputation
 */
public class OilTycoonCustomer implements SpecialCustomer {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        ShopcoreMod.MODID,
        "oil_tycoon"
    );

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Component displayName() {
        return Component.translatable("customer.shopcore.oil_tycoon.name");
    }

    @Override
    public boolean canSpawn(ShopInstance shop, ShopMenuSnapshot menu) {
        // Only spawns at Skilled tier or higher
        return shop.reputation() >= 50.0;
    }

    @Override
    public double spawnWeight(ShopInstance shop) {
        // Higher reputation = more likely to appear
        // 50 rep = 1.0x, 100 rep = 1.5x, 200 rep = 2.0x
        return 1.0 + (shop.reputation() - 50.0) / 100.0;
    }

    @Override
    public double priceModifier() {
        return 2.0; // Pays double
    }

    @Override
    public double reputationModifier() {
        return 1.5; // Gives 50% more reputation
    }

    @Override
    public Optional<Component> onOrderRecorded(
            ShopInstance shop,
            ShopOrder order,
            ServerPlayer player,
            LivingEntity entity
    ) {
        // Give gold ingot as tip
        if (player != null) {
            ItemStack tip = new ItemStack(Items.GOLD_INGOT, 1);
            if (!player.getInventory().add(tip)) {
                player.drop(tip, false);
            }
        }

        return Optional.of(Component.translatable("customer.shopcore.oil_tycoon.order_recorded"));
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
        return Optional.of(Component.translatable("customer.shopcore.oil_tycoon.order_completed"));
    }
}

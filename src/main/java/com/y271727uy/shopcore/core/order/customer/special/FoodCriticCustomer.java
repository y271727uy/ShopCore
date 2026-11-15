package com.y271727uy.shopcore.core.order.customer.special;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Example: Food Critic - Demanding customer who rewards perfection
 *
 * Characteristics:
 * - Only spawns at Expert tier (reputation >= 200)
 * - Requires high quality items (if Farmers Tales is installed)
 * - Pays normal price
 * - Gives massive reputation if order is perfect (100% delivered)
 * - Gives negative reputation if order is incomplete or low quality
 * - Has harsh dialogue for mistakes
 * - Must be served by player (requiresPlayerService = true)
 */
public class FoodCriticCustomer implements SpecialCustomer {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        ShopcoreMod.MODID,
        "food_critic"
    );

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public Component displayName() {
        return Component.translatable("customer.shopcore.food_critic.name");
    }

    @Override
    public boolean canSpawn(ShopInstance shop, ShopMenuSnapshot menu) {
        // Only spawns at Expert tier or higher
        return shop.reputation() >= 200.0;
    }

    @Override
    public double spawnWeight(ShopInstance shop) {
        // Rare spawn - 0.3x normal chance
        return 0.3;
    }

    @Override
    public double priceModifier() {
        return 1.0; // Normal price
    }

    @Override
    public double reputationModifier() {
        // Base modifier - will be adjusted based on quality in onOrderCompleted
        return 1.0;
    }

    @Override
    public boolean requiresPlayerService() {
        return true; // Must be served by player, not NPC
    }

    @Override
    public Optional<Component> onOrderRecorded(
            ShopInstance shop,
            ShopOrder order,
            ServerPlayer player,
            LivingEntity entity
    ) {
        return Optional.of(Component.translatable("customer.shopcore.food_critic.order_recorded"));
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
        // Calculate completion percentage
        int totalRequested = order.totalRequestedCount();
        int totalDelivered = order.totalDeliveredCount();
        double completionRate = (double) totalDelivered / totalRequested;

        // Check if order was perfect
        boolean isPerfect = completionRate >= 1.0 && order.qualityBonusReputation() > 0;

        if (isPerfect) {
            // Perfect order: 3x reputation boost
            // This needs to be handled in settlement service
            return Optional.of(Component.translatable(
                "customer.shopcore.food_critic.perfect",
                String.format("%.1f", finalReputation)
            ));
        } else if (completionRate >= 0.9) {
            // Good but not perfect
            return Optional.of(Component.translatable("customer.shopcore.food_critic.acceptable"));
        } else {
            // Poor service - harsh criticism
            return Optional.of(Component.translatable("customer.shopcore.food_critic.disappointed"));
        }
    }

    @Override
    public Optional<Component> onOrderFailed(
            ShopInstance shop,
            ShopOrder order,
            LivingEntity entity
    ) {
        return Optional.of(Component.translatable("customer.shopcore.food_critic.failed"));
    }

    /**
     * Calculate actual reputation modifier based on order quality
     * This should be called during settlement
     */
    public double getQualityBasedReputationModifier(ShopOrder order) {
        int totalRequested = order.totalRequestedCount();
        int totalDelivered = order.totalDeliveredCount();
        double completionRate = (double) totalDelivered / totalRequested;

        // Perfect order with quality bonus
        if (completionRate >= 1.0 && order.qualityBonusReputation() > 0) {
            return 3.0; // 3x reputation!
        }
        // Good completion
        else if (completionRate >= 0.9) {
            return 1.5; // 1.5x reputation
        }
        // Acceptable
        else if (completionRate >= 0.75) {
            return 1.0; // Normal reputation
        }
        // Poor - negative reputation
        else {
            return -0.5; // Lose reputation!
        }
    }
}

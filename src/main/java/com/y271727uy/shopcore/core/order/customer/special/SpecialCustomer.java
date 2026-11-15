package com.y271727uy.shopcore.core.order.customer.special;

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
 * Special customer with unique behaviors, modifiers, and dialogue.
 *
 * Special customers:
 * - Have spawn conditions based on shop state (reputation, menu, time, etc.)
 * - Can override/modify the order they place
 * - Have price and reputation modifiers
 * - Can have custom dialogue and interactions
 * - Can give rewards or have special effects
 *
 * Examples:
 * - Oil Tycoon: pays 2x price, gives 1.5x reputation, gives gold ingot
 * - Food Critic: requires high quality, gives 2x reputation if perfect
 * - Regular: fixed order, dialogue about knowing the owner
 */
public interface SpecialCustomer {

    /**
     * Unique identifier for this special customer type
     */
    ResourceLocation id();

    /**
     * Display name shown to players
     */
    Component displayName();

    /**
     * Check if this special customer can spawn at the current shop state
     *
     * @param shop current shop instance
     * @param menu current menu
     * @return true if can spawn
     */
    boolean canSpawn(ShopInstance shop, ShopMenuSnapshot menu);

    /**
     * Get spawn weight - higher weight = more likely to spawn
     * Only called if canSpawn returns true
     *
     * @param shop current shop instance
     * @return spawn weight (0.0 = never, 1.0 = normal, 2.0 = double chance)
     */
    default double spawnWeight(ShopInstance shop) {
        return 1.0;
    }

    /**
     * Override the order generation
     * If present, the returned order lines will be used instead of normal generation
     *
     * @param shop current shop instance
     * @param menu current menu
     * @return custom order lines, or empty to use normal generation
     */
    default Optional<OrderOverride> overrideOrder(ShopInstance shop, ShopMenuSnapshot menu) {
        return Optional.empty();
    }

    /**
     * Price modifier applied to the order
     *
     * @return multiplier (1.0 = normal, 2.0 = double price, 0.5 = half price)
     */
    default double priceModifier() {
        return 1.0;
    }

    /**
     * Reputation modifier applied to the order
     *
     * @return multiplier (1.0 = normal, 1.5 = 50% more reputation)
     */
    default double reputationModifier() {
        return 1.0;
    }

    /**
     * Called when this special customer spawns
     *
     * @param shop shop instance
     * @param entity customer entity (can be null if no entity)
     */
    default void onSpawn(ShopInstance shop, LivingEntity entity) {
        // Optional override
    }

    /**
     * Called when order is recorded/accepted
     *
     * @param shop shop instance
     * @param order the order
     * @param player player who accepted the order (can be null)
     * @param entity customer entity (can be null)
     * @return dialogue to show, or empty
     */
    default Optional<Component> onOrderRecorded(
            ShopInstance shop,
            ShopOrder order,
            ServerPlayer player,
            LivingEntity entity
    ) {
        return Optional.empty();
    }

    /**
     * Called when order is completed/delivered
     *
     * @param shop shop instance
     * @param order the completed order
     * @param player player who completed the order (can be null)
     * @param entity customer entity (can be null)
     * @param finalPrice final price paid
     * @param finalReputation final reputation earned
     * @return dialogue to show, or empty
     */
    default Optional<Component> onOrderCompleted(
            ShopInstance shop,
            ShopOrder order,
            ServerPlayer player,
            LivingEntity entity,
            long finalPrice,
            double finalReputation
    ) {
        return Optional.empty();
    }

    /**
     * Called when order fails or is cancelled
     *
     * @param shop shop instance
     * @param order the failed order
     * @param entity customer entity (can be null)
     * @return dialogue to show, or empty
     */
    default Optional<Component> onOrderFailed(
            ShopInstance shop,
            ShopOrder order,
            LivingEntity entity
    ) {
        return Optional.empty();
    }

    /**
     * Whether this special customer should be ignored by NPC employees
     * (i.e., must be served by player)
     */
    default boolean requiresPlayerService() {
        return false;
    }

    /**
     * Custom skin/texture for entity
     * Implementation depends on entity mod (TouhouLittleMaid, etc.)
     *
     * @return custom skin identifier, or empty for default
     */
    default Optional<String> customSkin() {
        return Optional.empty();
    }
}

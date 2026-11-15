package com.y271727uy.shopcore.core.order.customer.special;

import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and manager for special customers
 *
 * Handles:
 * - Registering special customer types
 * - Selecting special customers based on spawn conditions and weights
 * - Looking up special customers by ID
 */
public final class SpecialCustomerRegistry {

    private static final Map<ResourceLocation, SpecialCustomer> REGISTRY = new ConcurrentHashMap<>();

    private SpecialCustomerRegistry() {
    }

    /**
     * Register a special customer type
     *
     * @param customer special customer to register
     */
    public static void register(SpecialCustomer customer) {
        Objects.requireNonNull(customer, "customer");
        ResourceLocation id = customer.id();
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Special customer already registered: " + id);
        }
        REGISTRY.put(id, customer);
    }

    /**
     * Get a special customer by ID
     *
     * @param id special customer ID
     * @return special customer, or empty if not found
     */
    public static Optional<SpecialCustomer> get(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    /**
     * Get all registered special customers
     *
     * @return unmodifiable collection of all special customers
     */
    public static Collection<SpecialCustomer> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Attempt to select a special customer for spawning
     *
     * @param shop current shop instance
     * @param menu current menu
     * @param random random source
     * @param baseChance base chance for special customer to spawn (0.0 to 1.0)
     * @return selected special customer, or empty if none selected
     */
    public static Optional<SpecialCustomer> trySelect(
            ShopInstance shop,
            ShopMenuSnapshot menu,
            RandomSource random,
            double baseChance
    ) {
        if (random.nextDouble() >= baseChance) {
            return Optional.empty(); // Roll failed
        }

        // Build weighted list of eligible special customers
        List<WeightedCustomer> eligible = new ArrayList<>();
        for (SpecialCustomer customer : REGISTRY.values()) {
            if (customer.canSpawn(shop, menu)) {
                double weight = customer.spawnWeight(shop);
                if (weight > 0.0) {
                    eligible.add(new WeightedCustomer(customer, weight));
                }
            }
        }

        if (eligible.isEmpty()) {
            return Optional.empty();
        }

        // Select based on weights
        return Optional.of(selectWeighted(eligible, random));
    }

    /**
     * Weighted random selection
     */
    private static SpecialCustomer selectWeighted(List<WeightedCustomer> weighted, RandomSource random) {
        double totalWeight = weighted.stream()
            .mapToDouble(WeightedCustomer::weight)
            .sum();

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0.0;

        for (WeightedCustomer wc : weighted) {
            cumulative += wc.weight();
            if (roll < cumulative) {
                return wc.customer();
            }
        }

        // Fallback to last (should not happen)
        return weighted.get(weighted.size() - 1).customer();
    }

    /**
     * Clear all registrations (for testing)
     */
    public static void clear() {
        REGISTRY.clear();
    }

    private record WeightedCustomer(SpecialCustomer customer, double weight) {
    }
}

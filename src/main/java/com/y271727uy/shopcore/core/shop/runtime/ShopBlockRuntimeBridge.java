package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.customer.CustomerProfileSelector;
import com.y271727uy.shopcore.core.order.generation.OrderGenerationSchedule;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRuleSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ShopBlockRuntimeBridge {
    private final ShopRuntimeTickService tickService;

    public ShopBlockRuntimeBridge() {
        this(new ShopRuntimeTickService());
    }

    public ShopBlockRuntimeBridge(ShopRuntimeTickService tickService) {
        this.tickService = Objects.requireNonNull(tickService, "tickService");
    }

    public ShopRuntimeTickResult tick(ShopBlockRuntimeHolder holder, ShopBlockRuntimeTickInput input) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(input, "input");
        ShopRuntimeTickResult result = tickService.tick(new ShopRuntimeTickContext(
                holder.shopcore$shopInstance(),
                holder.shopcore$menuSnapshot(),
                input.demandPoolCatalog(),
                input.openingRuleSet(),
                holder.shopcore$orderBook(),
                input.customerProfile(),
                input.customerProfileSelector(),
                input.orderGenerationSchedule(),
                input.openingAttributes(),
                holder.shopcore$openRequested(),
                input.orderGenerationRequested(),
                input.reputation(),
                input.dayTime(),
                input.gameTime(),
                input.orderTtlTicks(),
                input.random()
        ));
        tickService.apply(holder, result);
        return result;
    }

    public record ShopBlockRuntimeTickInput(
            DemandPoolCatalog demandPoolCatalog,
            ShopOpeningRuleSet openingRuleSet,
            Optional<CustomerProfile> customerProfile,
            Optional<CustomerProfileSelector> customerProfileSelector,
            OrderGenerationSchedule orderGenerationSchedule,
            Map<ResourceLocation, Object> openingAttributes,
            boolean orderGenerationRequested,
            double reputation,
            long dayTime,
            long gameTime,
            long orderTtlTicks,
            RandomSource random
    ) {
        public ShopBlockRuntimeTickInput {
            Objects.requireNonNull(demandPoolCatalog, "demandPoolCatalog");
            Objects.requireNonNull(openingRuleSet, "openingRuleSet");
            customerProfile = Objects.requireNonNullElse(customerProfile, Optional.empty());
            customerProfileSelector = Objects.requireNonNullElse(customerProfileSelector, Optional.empty());
            orderGenerationSchedule = Objects.requireNonNullElse(orderGenerationSchedule, OrderGenerationSchedule.ALWAYS);
            openingAttributes = Map.copyOf(Objects.requireNonNull(openingAttributes, "openingAttributes"));
            if (Double.isNaN(reputation)) {
                reputation = 0.0D;
            }
            if (dayTime < 0L) {
                throw new IllegalArgumentException("dayTime cannot be negative");
            }
            if (gameTime < 0L) {
                throw new IllegalArgumentException("gameTime cannot be negative");
            }
            if (orderTtlTicks < 0L) {
                throw new IllegalArgumentException("orderTtlTicks cannot be negative");
            }
            Objects.requireNonNull(random, "random");
        }
    }
}

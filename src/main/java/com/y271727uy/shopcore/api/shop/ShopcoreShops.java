package com.y271727uy.shopcore.api.shop;

import com.y271727uy.shopcore.core.shop.runtime.ConfiguredShopRuntime;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/** Public entry point for runtime creation, shop queries and settlement accounts. */
public final class ShopcoreShops {
    private ShopcoreShops() { }

    public static ShopRuntime createRuntime(BlockEntity host, ShopDefinition definition,
                                            Supplier<Direction> facing, ShopFeedback feedback) {
        return new ConfiguredShopRuntime(host, definition, facing, feedback);
    }

    public static boolean isShop(BlockEntity blockEntity) {
        return blockEntity instanceof ShopRuntimeHost;
    }

    /** Read-only: an uninitialized runtime has no snapshot yet. */
    public static Optional<ShopSnapshot> snapshot(BlockEntity blockEntity) {
        return blockEntity instanceof ShopRuntimeHost host ? host.shopRuntime().snapshot() : Optional.empty();
    }

    public static ShopAccountBindingResult bindSettlementAccount(BlockEntity blockEntity, UUID accountUuid,
                                                                  boolean taxExempt) {
        Objects.requireNonNull(accountUuid, "accountUuid");
        return blockEntity instanceof ShopRuntimeHost host
                ? host.shopRuntime().bindSettlementAccount(accountUuid, taxExempt)
                : ShopAccountBindingResult.UNSUPPORTED_BLOCK_ENTITY;
    }

    public static ShopAccountBindingResult unbindSettlementAccount(BlockEntity blockEntity, UUID accountUuid) {
        Objects.requireNonNull(accountUuid, "accountUuid");
        return blockEntity instanceof ShopRuntimeHost host
                ? host.shopRuntime().unbindSettlementAccount(accountUuid)
                : ShopAccountBindingResult.UNSUPPORTED_BLOCK_ENTITY;
    }
}

package com.y271727uy.shopcore.api.component.economic;

import com.y271727uy.shopcore.core.economic.price.PriceRegistry;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/** Price queries return API values; no internal price or registry objects escape this boundary. */
public final class ShopcorePrices {
    private ShopcorePrices() { }

    public static Optional<PriceQuote> findPrice(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return PriceRegistry.findPrice(stack).filter(price -> !price.isEmpty())
                .map(price -> new PriceQuote(price.basicPrice(), price.addPrice(), price.reputation()));
    }

    public static boolean hasPrice(ItemStack stack) {
        return findPrice(stack).isPresent();
    }
}

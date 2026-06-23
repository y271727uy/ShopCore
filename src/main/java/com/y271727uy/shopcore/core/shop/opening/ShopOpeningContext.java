package com.y271727uy.shopcore.core.shop.opening;

import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ShopOpeningContext(
        ShopInstance shop,
        List<ShopListing> listings,
        Map<ResourceLocation, Object> attributes
) {
    public ShopOpeningContext {
        Objects.requireNonNull(shop, "shop");
        listings = List.copyOf(Objects.requireNonNull(listings, "listings"));
        attributes = Map.copyOf(Objects.requireNonNull(attributes, "attributes"));
    }

    public static ShopOpeningContext of(ShopInstance shop, List<ShopListing> listings) {
        return new ShopOpeningContext(shop, listings, Map.of());
    }

    public <T> Optional<T> get(ResourceLocation key, Class<T> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(type, "type");
        Object value = attributes.get(key);
        if (!type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(value));
    }
}

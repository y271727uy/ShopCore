package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CustomerProfileCatalog {
    private final Map<ResourceLocation, CustomerProfile> profiles = new LinkedHashMap<>();

    public CustomerProfile register(CustomerProfile profile) {
        Objects.requireNonNull(profile, "profile");
        profiles.put(profile.customerType(), profile);
        return profile;
    }

    public void registerAll(Collection<CustomerProfile> profiles) {
        Objects.requireNonNull(profiles, "profiles");
        for (CustomerProfile profile : profiles) {
            register(profile);
        }
    }

    public Optional<CustomerProfile> find(ResourceLocation customerType) {
        Objects.requireNonNull(customerType, "customerType");
        return Optional.ofNullable(profiles.get(customerType));
    }

    public List<CustomerProfile> profiles() {
        return List.copyOf(profiles.values());
    }

    public boolean isEmpty() {
        return profiles.isEmpty();
    }
}

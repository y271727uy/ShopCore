package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;

import java.util.Objects;
import java.util.Optional;

public class FixedCustomerSelector implements CustomerProfileSelector {
    private final CustomerProfile profile;

    public FixedCustomerSelector(CustomerProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    @Override
    public Optional<CustomerProfile> select(CustomerSelectionContext context) {
        Objects.requireNonNull(context, "context");
        return Optional.of(profile);
    }
}

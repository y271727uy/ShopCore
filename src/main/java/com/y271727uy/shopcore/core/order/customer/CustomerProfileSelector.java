package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;

import java.util.Optional;

public interface CustomerProfileSelector {
    Optional<CustomerProfile> select(CustomerSelectionContext context);
}

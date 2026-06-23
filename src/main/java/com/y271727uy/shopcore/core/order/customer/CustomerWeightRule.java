package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;

@FunctionalInterface
public interface CustomerWeightRule {
    double weight(CustomerSelectionContext context, CustomerProfile profile);
}

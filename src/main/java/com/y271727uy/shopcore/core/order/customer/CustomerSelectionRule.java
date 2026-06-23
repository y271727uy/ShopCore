package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;

@FunctionalInterface
public interface CustomerSelectionRule {
    boolean allows(CustomerSelectionContext context, CustomerProfile profile);
}

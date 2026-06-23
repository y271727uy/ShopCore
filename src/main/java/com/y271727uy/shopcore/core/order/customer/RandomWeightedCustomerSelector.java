package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;

import java.util.List;

public class RandomWeightedCustomerSelector extends AbstractWeightedCustomerSelector {
    public RandomWeightedCustomerSelector(List<CustomerProfile> profiles) {
        this(profiles, List.of(CustomerSelectionRules.acceptsAnyAvailableListing()), List.of());
    }

    public RandomWeightedCustomerSelector(
            List<CustomerProfile> profiles,
            List<CustomerSelectionRule> rules,
            List<CustomerWeightRule> weightRules
    ) {
        super(profiles, rules, weightRules);
    }
}

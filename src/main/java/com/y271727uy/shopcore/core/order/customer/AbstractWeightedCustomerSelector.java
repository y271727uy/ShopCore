package com.y271727uy.shopcore.core.order.customer;

import com.y271727uy.shopcore.core.order.CustomerProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractWeightedCustomerSelector implements CustomerProfileSelector {
    private final List<CustomerProfile> profiles;
    private final List<CustomerSelectionRule> rules;
    private final List<CustomerWeightRule> weightRules;

    protected AbstractWeightedCustomerSelector(
            List<CustomerProfile> profiles,
            List<CustomerSelectionRule> rules,
            List<CustomerWeightRule> weightRules
    ) {
        this.profiles = List.copyOf(Objects.requireNonNull(profiles, "profiles"));
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
        this.weightRules = List.copyOf(Objects.requireNonNull(weightRules, "weightRules"));
    }

    @Override
    public Optional<CustomerProfile> select(CustomerSelectionContext context) {
        Objects.requireNonNull(context, "context");
        List<WeightedProfile> candidates = new ArrayList<>();
        for (CustomerProfile profile : profiles) {
            if (!allows(context, profile)) {
                continue;
            }
            double weight = weight(context, profile);
            if (weight > 0.0D) {
                candidates.add(new WeightedProfile(profile, weight));
            }
        }
        return pick(context, candidates);
    }

    protected boolean allows(CustomerSelectionContext context, CustomerProfile profile) {
        for (CustomerSelectionRule rule : rules) {
            if (!rule.allows(context, profile)) {
                return false;
            }
        }
        return true;
    }

    protected double weight(CustomerSelectionContext context, CustomerProfile profile) {
        double weight = baseWeight(context, profile);
        for (CustomerWeightRule rule : weightRules) {
            weight *= rule.weight(context, profile);
        }
        return weight;
    }

    protected double baseWeight(CustomerSelectionContext context, CustomerProfile profile) {
        return 1.0D;
    }

    protected Optional<CustomerProfile> pick(CustomerSelectionContext context, List<WeightedProfile> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        double total = candidates.stream().mapToDouble(WeightedProfile::weight).sum();
        if (total <= 0.0D) {
            return Optional.empty();
        }
        double cursor = context.random().nextDouble() * total;
        for (WeightedProfile candidate : candidates) {
            cursor -= candidate.weight();
            if (cursor <= 0.0D) {
                return Optional.of(candidate.profile());
            }
        }
        return Optional.of(candidates.get(candidates.size() - 1).profile());
    }

    protected record WeightedProfile(CustomerProfile profile, double weight) {
        protected WeightedProfile {
            Objects.requireNonNull(profile, "profile");
            if (!Double.isFinite(weight) || weight < 0.0D) {
                throw new IllegalArgumentException("weight must be finite and non-negative");
            }
        }
    }
}

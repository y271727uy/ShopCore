package com.y271727uy.shopcore.core.order.evaluator;

import com.y271727uy.shopcore.core.market.tier.MarketTierConfig;
import com.y271727uy.shopcore.core.market.tier.MarketTierConfigs;
import com.y271727uy.shopcore.core.order.CustomerOrderSelector;
import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.CustomerProfiles;
import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.OrderSelectionContext;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.request.ItemListOrderRequest;
import com.y271727uy.shopcore.core.order.request.OrderRequest;
import com.y271727uy.shopcore.core.order.request.SingleItemOrderRequest;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Compact entry point for customer choice, demand generation and delivery evaluation.
 */
public final class CustomerOrderEvaluationService {
    private final CustomerOrderSelector orderSelector;
    private final OrderEvaluator evaluator;
    private final List<CustomerProfile> customerProfiles;

    public CustomerOrderEvaluationService() {
        this(new CustomerOrderSelector(), DefaultOrderEvaluator.INSTANCE, CustomerProfiles.defaults());
    }

    public CustomerOrderEvaluationService(
            CustomerOrderSelector orderSelector,
            OrderEvaluator evaluator,
            List<CustomerProfile> customerProfiles
    ) {
        this.orderSelector = Objects.requireNonNull(orderSelector, "orderSelector");
        this.evaluator = Objects.requireNonNull(evaluator, "evaluator");
        this.customerProfiles = List.copyOf(Objects.requireNonNull(customerProfiles, "customerProfiles"));
        if (this.customerProfiles.isEmpty()) {
            throw new IllegalArgumentException("customerProfiles cannot be empty");
        }
    }

    public Optional<CustomerOrderDecision> decide(CustomerOrderContext context) {
        Objects.requireNonNull(context, "context");
        Optional<CustomerProfile> selectedCustomer = selectCustomer(context);
        if (selectedCustomer.isEmpty()) {
            return Optional.empty();
        }

        CustomerProfile profile = selectedCustomer.get();
        OrderSelectionContext selectionContext = new OrderSelectionContext(
                context.shop().shopId(),
                context.shop().shopPos(),
                context.shop().shopTier(),
                context.shop().marketTier(),
                context.reputation(),
                context.listings(),
                profile,
                context.gameTime(),
                context.orderTtlTicks(),
                context.random()
        );

        return orderSelector.select(selectionContext)
                .map(order -> new CustomerOrderDecision(profile, order, toRequest(order)));
    }

    public OrderEvaluation evaluate(OrderRequest request, ItemStack deliveredStack) {
        Objects.requireNonNull(request, "request");
        if (!evaluator.supports(request.kind())) {
            return OrderEvaluation.of(
                    request.kind(),
                    0,
                    request.requestedCount(),
                    OrderEvaluationContext.DEFAULT_MINIMUM_ACCEPTED_SCORE,
                    OrderEvaluation.REASON_UNSUPPORTED_STRUCTURED
            );
        }
        return evaluator.evaluate(OrderEvaluationContext.exact(request, deliveredStack));
    }

    public Optional<CustomerProfile> selectCustomer(CustomerOrderContext context) {
        Objects.requireNonNull(context, "context");
        MarketTierConfig marketConfig = MarketTierConfigs.get(context.shop().marketTier());
        List<ScoredCustomer> scored = customerProfiles.stream()
                .filter(profile -> marketConfig.demandProfile().customerTypes().contains(profile.customerType()))
                .map(profile -> new ScoredCustomer(profile, scoreCustomer(profile, context, marketConfig)))
                .filter(candidate -> candidate.score() > 0)
                .sorted(Comparator.comparingInt(ScoredCustomer::score).reversed())
                .toList();

        if (scored.isEmpty()) {
            return Optional.empty();
        }

        int total = scored.stream().mapToInt(ScoredCustomer::score).sum();
        int cursor = context.random().nextInt(total);
        for (ScoredCustomer candidate : scored) {
            cursor -= candidate.score();
            if (cursor < 0) {
                return Optional.of(candidate.profile());
            }
        }
        return Optional.of(scored.get(scored.size() - 1).profile());
    }

    private static int scoreCustomer(CustomerProfile profile, CustomerOrderContext context, MarketTierConfig marketConfig) {
        int score = 0;
        for (ShopListing listing : context.listings()) {
            if (!listing.enabled()) {
                continue;
            }
            if (!marketConfig.demandProfile().demandCategories().contains(listing.demandCategory())) {
                continue;
            }
            if (!marketConfig.allowsComplexity(listing.complexity())) {
                continue;
            }
            if (!profile.acceptsCategory(listing.demandCategory()) || !profile.acceptsComplexity(listing.complexity())) {
                continue;
            }
            score += 1;
            if (profile.prefersCategory(listing.demandCategory())) {
                score += 2;
            }
            if (profile.prefersComplexity(listing.complexity())) {
                score += 1;
            }
        }
        return score;
    }

    private static OrderRequest toRequest(ShopOrder order) {
        if (order.lines().size() == 1) {
            OrderLine line = order.lines().get(0);
            return new SingleItemOrderRequest(line.requestedItem(), line.requestedCount(), line.unitPrice());
        }
        return new ItemListOrderRequest(order.lines());
    }

    private record ScoredCustomer(CustomerProfile profile, int score) {
    }
}

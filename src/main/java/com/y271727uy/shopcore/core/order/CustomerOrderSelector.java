package com.y271727uy.shopcore.core.order;

import com.y271727uy.shopcore.api.economic.ShopcorePrices;
import com.y271727uy.shopcore.core.menu.MenuItemCandidates;
import com.y271727uy.shopcore.core.market.tier.MarketTierConfig;
import com.y271727uy.shopcore.core.market.tier.MarketTierConfigs;
import com.y271727uy.shopcore.core.unlock.FeatureAccessContext;
import com.y271727uy.shopcore.core.unlock.FeatureAccessService;
import com.y271727uy.shopcore.core.unlock.FeatureKey;
import com.y271727uy.shopcore.economic.price.Price;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Converts shop supply + customer demand into one order.
 */
public final class CustomerOrderSelector {
    private final OrderCandidateScorer scorer;

    public CustomerOrderSelector() {
        this(RuleBasedOrderCandidateScorer.INSTANCE);
    }

    public CustomerOrderSelector(OrderCandidateScorer scorer) {
        this.scorer = Objects.requireNonNull(scorer, "scorer");
    }

    public Optional<ShopOrder> select(OrderSelectionContext context) {
        Objects.requireNonNull(context, "context");

        List<ScoredCandidate> scored = buildCandidates(context).stream()
                .map(candidate -> new ScoredCandidate(candidate, scoreWithJitter(context, candidate)))
                .filter(candidate -> candidate.score() > 0.0D)
                .sorted(Comparator.comparingDouble(ScoredCandidate::score).reversed())
                .toList();

        if (scored.isEmpty()) {
            return Optional.empty();
        }

        OrderCandidate selected = weightedPick(context, scored).candidate();
        return Optional.of(ShopOrder.pending(
                context.shopId(),
                context.shopPos(),
                context.customerProfile().customerType(),
                List.of(selected.toOrderLine()),
                context.gameTime(),
                context.orderTtlTicks()
        ));
    }

    public List<OrderCandidate> buildCandidates(OrderSelectionContext context) {
        Objects.requireNonNull(context, "context");
        MarketTierConfig marketConfig = MarketTierConfigs.get(context.marketTier());
        FeatureAccessContext accessContext = FeatureAccessContext.of(context.shopTier(), context.marketTier(), context.reputation());

        List<OrderCandidate> candidates = new ArrayList<>();
        for (ShopListing listing : context.listings()) {
            if (!listing.enabled() || listing.availableForOrder() < context.customerProfile().minQuantity()) {
                continue;
            }
            if (!marketConfig.demandProfile().demandCategories().contains(listing.demandCategory())) {
                continue;
            }
            if (!marketConfig.allowsComplexity(listing.complexity())) {
                continue;
            }
            if (!context.customerProfile().acceptsCategory(listing.demandCategory())
                    || !context.customerProfile().acceptsComplexity(listing.complexity())) {
                continue;
            }
            if (!featureAllowed(accessContext, listing.complexity())) {
                continue;
            }

            List<ItemStack> menuItems = MenuItemCandidates.resolve(listing.menuId());

            for (ItemStack stack : menuItems) {
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemId == null) {
                    continue;
                }

                int unitPrice = unitPrice(listing, stack);
                if (unitPrice <= 0) {
                    continue;
                }

                int quantity = chooseQuantity(context, listing, unitPrice);
                if (quantity <= 0) {
                    continue;
                }

                candidates.add(new OrderCandidate(
                        listing,
                        stack,
                        itemId,
                        listing.demandCategory(),
                        listing.complexity(),
                        quantity,
                        unitPrice
                ));
            }
        }
        return List.copyOf(candidates);
    }

    private double scoreWithJitter(OrderSelectionContext context, OrderCandidate candidate) {
        double base = scorer.score(context, candidate);
        if (base <= 0.0D) {
            return 0.0D;
        }
        return base * (0.85D + context.random().nextDouble() * 0.30D);
    }

    private ScoredCandidate weightedPick(OrderSelectionContext context, List<ScoredCandidate> candidates) {
        double total = candidates.stream().mapToDouble(ScoredCandidate::score).sum();
        if (total <= 0.0D) {
            return candidates.get(0);
        }

        double cursor = context.random().nextDouble() * total;
        for (ScoredCandidate candidate : candidates) {
            cursor -= candidate.score();
            if (cursor <= 0.0D) {
                return candidate;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private static int unitPrice(ShopListing listing, ItemStack stack) {
        if (listing.pricingMode() == PricingMode.MANUAL) {
            return listing.manualUnitPrice();
        }

        Price price = ShopcorePrices.getPrice(stack);
        return price.totalPrice();
    }

    private static int chooseQuantity(OrderSelectionContext context, ShopListing listing, int unitPrice) {
        CustomerProfile profile = context.customerProfile();
        int maxByBudget = unitPrice <= 0 ? 0 : profile.maxBudget() / unitPrice;
        int max = Math.min(Math.min(profile.maxQuantity(), listing.availableForOrder()), maxByBudget);
        int min = Math.max(1, profile.minQuantity());
        if (max < min) {
            return 0;
        }

        return min + context.random().nextInt(max - min + 1);
    }

    private static boolean featureAllowed(FeatureAccessContext context, com.y271727uy.shopcore.core.market.demand.OrderComplexity complexity) {
        FeatureKey key = switch (complexity) {
            case SINGLE_ITEM -> null;
            case QUALITY_ITEM -> FeatureKey.QUALITY_ORDERS;
            case MULTI_LINE -> FeatureKey.MULTI_LINE_ORDERS;
            case TIMED -> FeatureKey.TIMED_ORDERS;
            case BULK -> FeatureKey.BULK_ORDERS;
            case RARE -> FeatureKey.RARE_CUSTOMERS;
        };
        return key == null || FeatureAccessService.canUse(context, key).allowed();
    }

    private record ScoredCandidate(OrderCandidate candidate, double score) {
    }
}

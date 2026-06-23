package com.y271727uy.shopcore.core.shop.operation;

import com.y271727uy.shopcore.core.menu.MenuItemCandidates;
import com.y271727uy.shopcore.core.menu.ShopMenuListingResolver;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolSelectionContext;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningContext;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningResult;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningValidator;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyContext;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyDecision;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyEvaluator;
import com.y271727uy.shopcore.core.shop.tier.ShopTierConfig;
import com.y271727uy.shopcore.core.shop.tier.ShopTierConfigs;

import java.util.List;
import java.util.Objects;

public class ShopOperationService {
    public ShopOperationResult apply(ShopOperationContext context) {
        Objects.requireNonNull(context, "context");

        if (!context.openRequested()) {
            if (context.shop().isOpen()) {
                return ShopOperationResult.of(
                        ShopOperationStatus.CLOSED,
                        context.shop().close(context.gameTime()),
                        List.of(),
                        null,
                        null,
                        ShopOperationResult.REASON_OPEN_REQUEST_DISABLED
                );
            }
            return ShopOperationResult.of(
                    ShopOperationStatus.ALREADY_CLOSED,
                    context.shop(),
                    List.of(),
                    null,
                    null,
                    ShopOperationResult.REASON_ALREADY_CLOSED
            );
        }

        if (context.menuSnapshot().isEmpty()) {
            return ShopOperationResult.of(
                    ShopOperationStatus.DENIED_BY_MENU,
                    context.shop(),
                    List.of(),
                    null,
                    null,
                    ShopOperationResult.REASON_EMPTY_MENU
            );
        }

        List<ShopListing> resolvedListings = resolveListings(context);
        if (resolvedListings.isEmpty()) {
            return ShopOperationResult.of(
                    ShopOperationStatus.DENIED_BY_MENU,
                    context.shop(),
                    List.of(),
                    null,
                    null,
                    ShopOperationResult.REASON_NO_RESOLVED_LISTINGS
            );
        }
        if (resolvedListings.stream().noneMatch(this::hasItemCandidates)) {
            return ShopOperationResult.of(
                    ShopOperationStatus.DENIED_BY_MENU,
                    context.shop(),
                    resolvedListings,
                    null,
                    null,
                    ShopOperationResult.REASON_NO_ITEM_CANDIDATES
            );
        }

        ShopOpeningResult openingResult = ShopOpeningValidator.validate(
                ShopOpeningContext.of(context.shop(), resolvedListings),
                context.openingRuleSet()
        );
        if (!openingResult.allowedToOpen()) {
            return ShopOperationResult.of(
                    ShopOperationStatus.DENIED_BY_OPENING_RULE,
                    context.shop(),
                    resolvedListings,
                    openingResult,
                    null,
                    ShopOperationStatus.DENIED_BY_OPENING_RULE.name().toLowerCase()
            );
        }

        ShopTierConfig tierConfig = ShopTierConfigs.get(context.shop().shopTier());
        if (tierConfig != null && context.orderBook().activeOrderCount() >= tierConfig.capacity().pendingOrderLimit()) {
            return ShopOperationResult.of(
                    ShopOperationStatus.DENIED_BY_ORDER_CAPACITY,
                    context.shop(),
                    resolvedListings,
                    openingResult,
                    null,
                    ShopOperationResult.REASON_ORDER_CAPACITY_FULL
            );
        }

        OperatingPolicyDecision policyDecision = OperatingPolicyEvaluator.evaluate(new OperatingPolicyContext(
                context.shop().operatingPolicy(),
                context.dayTime(),
                context.openRequested()
        ));
        if (!policyDecision.shouldOpen()) {
            return ShopOperationResult.of(
                    ShopOperationStatus.DENIED_BY_POLICY,
                    context.shop(),
                    resolvedListings,
                    openingResult,
                    policyDecision,
                    policyDecision.reason()
            );
        }

        if (context.shop().isOpen()) {
            return ShopOperationResult.of(
                    ShopOperationStatus.ALREADY_OPEN,
                    context.shop(),
                    resolvedListings,
                    openingResult,
                    policyDecision,
                    ShopOperationResult.REASON_ALREADY_OPEN
            );
        }

        return ShopOperationResult.of(
                ShopOperationStatus.OPENED,
                context.shop().open(context.gameTime()),
                resolvedListings,
                openingResult,
                policyDecision,
                ShopOperationResult.REASON_OPENED
        );
    }

    private List<ShopListing> resolveListings(ShopOperationContext context) {
        ShopMenuListingResolver resolver = new ShopMenuListingResolver(context.demandPoolCatalog());
        DemandPoolSelectionContext selectionContext = new DemandPoolSelectionContext(
                context.shop().shopId(),
                context.shop().shopTier(),
                context.shop().marketTier(),
                context.gameTime(),
                context.random()
        );
        return resolver.resolve(context.menuSnapshot(), selectionContext);
    }

    private boolean hasItemCandidates(ShopListing listing) {
        if (!listing.enabled()) {
            return false;
        }
        try {
            return !MenuItemCandidates.resolve(listing.menuId()).isEmpty();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}

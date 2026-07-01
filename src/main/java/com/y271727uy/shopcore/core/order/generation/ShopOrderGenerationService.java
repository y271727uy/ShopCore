package com.y271727uy.shopcore.core.order.generation;

import com.y271727uy.shopcore.core.menu.ShopMenuListingResolver;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolSelectionContext;
import com.y271727uy.shopcore.core.order.CustomerOrderSelector;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.shop.diagnostic.ShopDiagnosticCode;
import com.y271727uy.shopcore.core.shop.tier.ShopTierConfigs;
import com.y271727uy.shopcore.economic.algorithm.micromachinelearning.helper.OrderDemandLearning;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ShopOrderGenerationService {
    private final CustomerOrderSelector orderSelector;

    public ShopOrderGenerationService() {
        this(new CustomerOrderSelector(OrderDemandLearning.orderCandidateScorer()));
    }

    public ShopOrderGenerationService(CustomerOrderSelector orderSelector) {
        this.orderSelector = Objects.requireNonNull(orderSelector, "orderSelector");
    }

    public ShopOrderGenerationResult generate(ShopOrderGenerationContext context) {
        Objects.requireNonNull(context, "context");
        if (!context.generationRequested()) {
            return ShopOrderGenerationResult.empty(
                    ShopOrderGenerationStatus.SKIPPED,
                    List.of(),
                    ShopDiagnosticCode.ORDER_GENERATION_SKIPPED
            );
        }
        if (!context.shop().canAcceptOrders()) {
            return ShopOrderGenerationResult.empty(
                    ShopOrderGenerationStatus.CLOSED,
                    List.of(),
                    ShopDiagnosticCode.ORDER_GENERATION_CLOSED
            );
        }
        int orderLimit = ShopTierConfigs.get(context.shop().shopTier()).capacity().pendingOrderLimit();
        if (context.orderBook().activeOrderCount() >= orderLimit) {
            return ShopOrderGenerationResult.empty(
                    ShopOrderGenerationStatus.ORDER_CAPACITY_FULL,
                    List.of(),
                    ShopDiagnosticCode.ORDER_CAPACITY_FULL
            );
        }

        List<ShopListing> listings = resolveListings(context);
        if (listings.isEmpty()) {
            return ShopOrderGenerationResult.empty(
                    ShopOrderGenerationStatus.NO_LISTINGS,
                    listings,
                    ShopDiagnosticCode.ORDER_GENERATION_NO_LISTINGS
            );
        }

        Optional<ShopOrder> order = orderSelector.select(context.toSelectionContext(listings));
        return order.map(shopOrder -> ShopOrderGenerationResult.generated(shopOrder, listings))
                .orElseGet(() -> ShopOrderGenerationResult.empty(
                        ShopOrderGenerationStatus.NO_CANDIDATE,
                        listings,
                        ShopDiagnosticCode.ORDER_GENERATION_NO_CANDIDATE
                ));
    }

    private List<ShopListing> resolveListings(ShopOrderGenerationContext context) {
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
}

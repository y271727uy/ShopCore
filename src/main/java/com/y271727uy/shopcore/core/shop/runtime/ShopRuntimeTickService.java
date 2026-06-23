package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.core.order.CustomerProfile;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.customer.CustomerSelectionContext;
import com.y271727uy.shopcore.core.order.generation.OrderGenerationScheduleContext;
import com.y271727uy.shopcore.core.order.generation.ShopOrderGenerationContext;
import com.y271727uy.shopcore.core.order.generation.ShopOrderGenerationResult;
import com.y271727uy.shopcore.core.order.generation.ShopOrderGenerationService;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleResult;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleService;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleStatus;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.operation.ShopOperationContext;
import com.y271727uy.shopcore.core.shop.operation.ShopOperationResult;
import com.y271727uy.shopcore.core.shop.operation.ShopOperationService;
import com.y271727uy.shopcore.core.shop.policy.OperatingPolicyDecision;
import com.y271727uy.shopcore.core.shop.session.ShopSessionTickResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ShopRuntimeTickService {
    private final ShopOperationService operationService;
    private final ShopOrderGenerationService orderGenerationService;

    public ShopRuntimeTickService() {
        this(new ShopOperationService(), new ShopOrderGenerationService());
    }

    public ShopRuntimeTickService(ShopOperationService operationService, ShopOrderGenerationService orderGenerationService) {
        this.operationService = Objects.requireNonNull(operationService, "operationService");
        this.orderGenerationService = Objects.requireNonNull(orderGenerationService, "orderGenerationService");
    }

    public ShopRuntimeTickResult tick(ShopRuntimeTickContext context) {
        Objects.requireNonNull(context, "context");

        ShopOperationResult operationResult = operationService.apply(new ShopOperationContext(
                context.shop(),
                context.menuSnapshot(),
                context.demandPoolCatalog(),
                context.openingRuleSet(),
                context.orderBook(),
                context.openRequested(),
                context.dayTime(),
                context.gameTime(),
                context.random()
        ));

        Optional<ShopOrderGenerationResult> generationResult = Optional.empty();
        Optional<ShopOrder> incomingOrder = Optional.empty();
        Optional<CustomerProfile> customerProfile = selectCustomerProfile(context, operationResult);
        if (operationResult.shop().canAcceptOrders() && customerProfile.isPresent()) {
            boolean generationRequested = context.orderGenerationRequested()
                    && context.orderGenerationSchedule().evaluate(new OrderGenerationScheduleContext(
                    operationResult.shop(),
                    context.orderBook(),
                    context.dayTime(),
                    context.gameTime(),
                    context.random()
            )).shouldAttempt();
            ShopOrderGenerationResult generated = orderGenerationService.generate(new ShopOrderGenerationContext(
                    operationResult.shop(),
                    context.menuSnapshot(),
                    context.demandPoolCatalog(),
                    context.orderBook(),
                    customerProfile.get(),
                    context.reputation(),
                    generationRequested,
                    context.gameTime(),
                    context.orderTtlTicks(),
                    context.random()
            ));
            generationResult = Optional.of(generated);
            incomingOrder = generated.orderOptional();
        }

        ShopSessionTickResult sessionResult = refreshSession(context, operationResult, incomingOrder);

        return new ShopRuntimeTickResult(
                sessionResult.shop(),
                sessionResult.orderBook(),
                operationResult,
                generationResult,
                sessionResult,
                ShopRuntimeTickResult.diagnosticFor(operationResult, generationResult)
        );
    }

    public void apply(ShopBlockRuntimeHolder holder, ShopRuntimeTickResult result) {
        Objects.requireNonNull(holder, "holder");
        Objects.requireNonNull(result, "result");
        holder.shopcore$setShopInstance(result.shop());
        holder.shopcore$setOrderBook(result.orderBook());
    }

    private ShopSessionTickResult refreshSession(
            ShopRuntimeTickContext context,
            ShopOperationResult operationResult,
            Optional<ShopOrder> incomingOrder
    ) {
        ShopInstance shop = operationResult.shop();
        ShopOrderBook orderBook = context.orderBook();
        List<OrderLifecycleResult> events = new ArrayList<>();

        for (ShopOrder order : orderBook.orders()) {
            OrderLifecycleResult result = OrderLifecycleService.refresh(order, context.gameTime());
            if (result.countsAsExpired()) {
                shop = shop.withCurrentSession(shop.currentSession().recordOrderExpired());
            }
            if (result.afterOrder() != null) {
                orderBook = orderBook.replace(result.afterOrder());
            }
            if (result.status() != OrderLifecycleStatus.UNCHANGED) {
                events.add(result);
            }
        }

        if (incomingOrder.isPresent()) {
            OrderLifecycleResult createResult = OrderLifecycleService.tryCreate(shop, orderBook.activeOrders(), incomingOrder.get());
            events.add(createResult);
            if (createResult.countsAsCreated() && createResult.afterOrder() != null) {
                orderBook = orderBook.add(createResult.afterOrder());
                shop = shop.withCurrentSession(shop.currentSession().recordOrderCreated());
            }
        }

        OperatingPolicyDecision operatingDecision = operationResult.policyDecisionOptional().orElse(fallbackDecision(shop));
        return new ShopSessionTickResult(
                shop,
                orderBook,
                operatingDecision,
                operationResult.openingResultOptional(),
                events
        );
    }

    private OperatingPolicyDecision fallbackDecision(ShopInstance shop) {
        return shop.isOpen()
                ? OperatingPolicyDecision.open(shop.operatingPolicy(), "runtime_open")
                : OperatingPolicyDecision.closed(shop.operatingPolicy(), "runtime_closed");
    }

    private Optional<CustomerProfile> selectCustomerProfile(ShopRuntimeTickContext context, ShopOperationResult operationResult) {
        if (context.customerProfile().isPresent()) {
            return context.customerProfile();
        }
        return context.customerProfileSelector()
                .flatMap(selector -> selector.select(new CustomerSelectionContext(
                        operationResult.shop(),
                        operationResult.resolvedListings(),
                        context.reputation(),
                        context.dayTime(),
                        context.gameTime(),
                        context.random()
                )));
    }
}

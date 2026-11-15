package com.y271727uy.shopcore.core.shop.runtime;

import com.y271727uy.shopcore.api.component.order.DeliveryReceipt;
import com.y271727uy.shopcore.api.shop.ShopAccountBindingResult;
import com.y271727uy.shopcore.api.shop.ShopDefinition;
import com.y271727uy.shopcore.api.shop.ShopFeedback;
import com.y271727uy.shopcore.api.shop.ShopRuntime;
import com.y271727uy.shopcore.api.shop.ShopSnapshot;
import com.y271727uy.shopcore.api.shop.ShopStatus;
import com.y271727uy.shopcore.core.consumer.common.entity.OverheadConsumerLifecycle;
import com.y271727uy.shopcore.core.consumer.common.movement.ConsumerMovements;
import com.y271727uy.shopcore.core.consumer.common.runtime.ConsumerOrder;
import com.y271727uy.shopcore.core.consumer.common.runtime.ConsumerPopulation;
import com.y271727uy.shopcore.core.consumer.common.runtime.ShopConsumerRuntime;
import com.y271727uy.shopcore.core.consumer.eazy_npc.player.EazyNpcConsumerActor;
import com.y271727uy.shopcore.core.consumer.eazy_npc.player.EazyNpcConsumerMemory;
import com.y271727uy.shopcore.core.consumer.eazy_npc.player.EazyNpcConsumerNavigator;
import com.y271727uy.shopcore.core.consumer.eazy_npc.player.EazyNpcHumanoidConsumers;
import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import com.y271727uy.shopcore.core.menu.ListingMenuEntry;
import com.y271727uy.shopcore.core.menu.ShopMenuEntry;
import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.order.CustomerProfiles;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.generation.FixedIntervalOrderGenerationSchedule;
import com.y271727uy.shopcore.core.order.interaction.CustomerOrderPresentation;
import com.y271727uy.shopcore.core.order.interaction.DeliveryMessages;
import com.y271727uy.shopcore.core.order.interaction.HeldItemDeliveryInteraction;
import com.y271727uy.shopcore.core.order.lifecycle.OrderBookLifecycleService;
import com.y271727uy.shopcore.core.order.lifecycle.OrderLifecycleCommit;
import com.y271727uy.shopcore.core.order.lifecycle.OrderStateChange;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRuleSet;
import com.y271727uy.shopcore.core.shop.opening.rule.MinListingCountRule;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import com.y271727uy.shopcore.api.component.order.DeliveryResult;
import com.y271727uy.shopcore.api.component.order.OrderSnapshot;
import com.y271727uy.shopcore.core.order.interaction.OrderApiSnapshots;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Internal assembly behind both public presets and public component configuration. */
public final class ConfiguredShopRuntime implements ShopRuntime, ShopBlockRuntimeHolder {
    private static final String RUNTIME_TAG = "ShopRuntime";
    private final BlockEntity host;
    private final ShopDefinition definition;
    private final Supplier<Direction> facing;
    private final ShopBlockRuntimeBridge bridge = new ShopBlockRuntimeBridge();
    private final DemandPoolCatalog demandPools = new DemandPoolCatalog();
    private final EazyNpcConsumerMemory memory = new EazyNpcConsumerMemory();
    private final ShopConsumerRuntime<EazyNpcConsumerActor> consumers;
    private final HeldItemDeliveryInteraction delivery;
    private final ShopLifecycleController<ServerLevel> lifecycle;
    private final ShopOpeningRuleSet openingRules;
    private ShopInstance shop;
    private ShopMenuSnapshot menu;
    private ShopOrderBook orders;
    private boolean openRequested;
    private ShopRuntimeTickResult lastTickResult;

    public ConfiguredShopRuntime(BlockEntity host, ShopDefinition definition, Supplier<Direction> facing,
                                 ShopFeedback feedback) {
        this.host = Objects.requireNonNull(host, "host");
        this.definition = Objects.requireNonNull(definition, "definition");
        this.facing = Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(feedback, "feedback");
        openingRules = ShopOpeningRuleSet.of(definition.id(),
                List.of(new MinListingCountRule(definition.minimumListings())));
        consumers = new ShopConsumerRuntime<>(memory, new EazyNpcConsumerNavigator(),
                new OverheadConsumerLifecycle<EazyNpcConsumerActor>(definition.consumers().orderWaitTicks(),
                        definition.consumers().waitingText(), this::shopcore$orderBook, () -> {
                            if (host.getLevel() instanceof ServerLevel level) {
                                OverheadConsumerLifecycle.clearNearbyPlayerHud(level, host.getBlockPos());
                            }
                        }) {
                    @Override
                    public void arrived(EazyNpcConsumerActor actor) { feedback.arrived(actor.entity()); }
                    @Override
                    public void departing(EazyNpcConsumerActor actor) { feedback.departing(actor.entity()); }
                }, host::setChanged);
        delivery = new HeldItemDeliveryInteraction(this, memory, this::initialize, this::changedAndSync,
                CustomerOrderPresentation.OVERHEAD, new DeliveryMessages(definition.deliveryMessagePrefix()),
                (player, receipt) -> feedback.delivered(player,
                        new DeliveryReceipt(receipt.inputBefore(), receipt.deliveredCount())));
        lifecycle = new ShopLifecycleController<>(definition.runtimeIntervalTicks(),
                new ShopLifecycleController.Hooks<ServerLevel>() {
                    public void initialize() { ConfiguredShopRuntime.this.initialize(); }
                    public boolean openRequested() { return openRequested; }
                    public void setOpenRequested(boolean value) { openRequested = value; }
                    public void tickRuntime(ServerLevel level) { updateRuntime(level); }
                    public void tickComponents(ServerLevel level) {
                        consumers.tick(population(level), ConsumerOrder.activeOrders(orders), shop != null && shop.isOpen(),
                                ConsumerMovements.horizontalQueue(host.getBlockPos(), facing.get()),
                                level.getGameTime(), ConfiguredShopRuntime.this::changedAndSync);
                    }
                    public void cancelActiveOrders() {
                        ConfiguredShopRuntime.this.initialize();
                        var result = OrderBookLifecycleService.cancelActive(shop, orders);
                        OrderLifecycleCommit.apply(ConfiguredShopRuntime.this, result.shop(), result.orderBook());
                    }
                    public void closeComponents(ServerLevel level) { consumers.close(population(level)); }
                    public void reopenComponents(ServerLevel level) { consumers.reopen(population(level)); }
                    public void detachComponents(ServerLevel level) { consumers.detach(population(level)); }
                    public void changed() { changedAndSync(); }
                });
    }

    private ConsumerPopulation<EazyNpcConsumerActor> population(ServerLevel level) {
        return EazyNpcHumanoidConsumers.population(level, host.getBlockPos(), memory);
    }

    private void initialize() {
        ShopRuntimePersistence.initializeIfMissing(this, host.getBlockPos());
        // Preserve the existing shop's empty-menu fallback, including old saves.
        if (menu.isEmpty()) {
            var entries = new ArrayList<ShopMenuEntry>();
            var listings = List.copyOf(definition.menu().listings());
            for (int slot = 0; slot < listings.size(); slot++) {
                var listing = listings.get(slot);
                entries.add(ListingMenuEntry.of(ShopListing.manual(slot, listing.reference(),
                        DemandCategoryKey.BASIC_GOODS, OrderComplexity.SINGLE_ITEM,
                        listing.unitPrice(), listing.maxPerOrder(), ShopListing.UNKNOWN_STOCK)));
            }
            menu = menu.withEntries(entries);
        }
    }

    private void updateRuntime(ServerLevel level) {
        boolean missing = shop == null || menu == null || orders == null;
        initialize();
        lastTickResult = bridge.tick(this, new ShopBlockRuntimeBridge.ShopBlockRuntimeTickInput(
                demandPools, openingRules, Optional.of(CustomerProfiles.COMMON), Optional.empty(),
                FixedIntervalOrderGenerationSchedule.every(definition.orders().generationIntervalTicks()),
                Map.of(), true, 0.0D, level.getDayTime(), level.getGameTime(),
                definition.orders().lifetimeTicks(), level.random));
        if (missing) changedAndSync();
    }

    private void changedAndSync() {
        host.setChanged();
        var level = host.getLevel();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(host.getBlockPos(), host.getBlockState(), host.getBlockState(), 3);
        }
    }

    @Override
    public void tick(ServerLevel level) { lifecycle.tick(level, level.getGameTime()); }
    @Override
    public boolean toggleOpenRequested() {
        return lifecycle.toggleOpenRequested(host.getLevel() instanceof ServerLevel level ? level : null);
    }
    @Override
    public void removed(ServerLevel level) { lifecycle.removed(level); }
    @Override
    public ShopStatus status() {
        initialize();
        return new ShopStatus(openRequested, shop.isOpen(), orders.activeOrderCount(), orders.orders().size(),
                consumers.size(), lastTickResult == null ? "not_ticked" : lastTickResult.diagnostic().translationKey());
    }
    @Override
    public Optional<ShopSnapshot> snapshot() {
        if (shop == null) return Optional.empty();
        return Optional.of(new ShopSnapshot(shop.shopId().value(), shop.shopPos(), shop.shopTier().name(),
                shop.marketTier().name(), shop.operatingState().name(), shop.reputation(),
                shop.settlementAccountUuid(), shop.settlementTaxExempt(), orders == null ? 0 : orders.activeOrderCount()));
    }
    @Override
    public List<OrderSnapshot> orders() {
        initialize();
        return orders.orders().stream().map(OrderApiSnapshots::order).toList();
    }
    @Override
    public DeliveryResult deliver(ServerPlayer player, Entity consumer, ItemStack input) {
        return delivery.deliver(player, consumer, input);
    }
    @Override
    public boolean ownsConsumer(UUID consumerId) { return consumers.ownsConsumer(consumerId); }
    @Override
    public void consumerRemoved(UUID consumerId) { consumers.consumerRemoved(consumerId); }
    @Override
    public ShopAccountBindingResult bindSettlementAccount(UUID accountUuid, boolean taxExempt) {
        Objects.requireNonNull(accountUuid, "accountUuid");
        initialize();
        if (shop.isSettlementAccount(accountUuid)) return ShopAccountBindingResult.ALREADY_BOUND_TO_ACCOUNT;
        if (shop.hasSettlementAccount()) return ShopAccountBindingResult.BOUND_TO_OTHER_ACCOUNT;
        shop = shop.withSettlementAccount(accountUuid, taxExempt);
        changedAndSync();
        return ShopAccountBindingResult.BOUND;
    }
    @Override
    public ShopAccountBindingResult unbindSettlementAccount(UUID accountUuid) {
        Objects.requireNonNull(accountUuid, "accountUuid");
        initialize();
        if (!shop.hasSettlementAccount()) return ShopAccountBindingResult.NO_ACCOUNT_BOUND;
        if (!shop.isSettlementAccount(accountUuid)) return ShopAccountBindingResult.BOUND_TO_OTHER_ACCOUNT;
        shop = shop.withoutSettlementAccount();
        changedAndSync();
        return ShopAccountBindingResult.UNBOUND;
    }
    @Override
    public void load(CompoundTag tag) {
        handleUpdateTag(tag);
        consumers.load(tag, definition.consumers().storageKey());
        lifecycle.requestRuntimeTick();
    }
    @Override
    public void save(CompoundTag tag) {
        tag.merge(updateTag());
        consumers.save(tag, definition.consumers().storageKey());
    }
    @Override
    public CompoundTag updateTag() {
        ShopRuntimePersistence.initializeIfMissing(this, host.getBlockPos());
        CompoundTag tag = new CompoundTag();
        tag.put(RUNTIME_TAG, ShopRuntimePersistence.save(this));
        return tag;
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag.contains(RUNTIME_TAG)) {
            ShopRuntimePersistence.load(this, tag.getCompound(RUNTIME_TAG), host.getBlockPos());
        }
    }
    @Override
    public ShopInstance shopcore$shopInstance() { return shop; }
    @Override
    public void shopcore$setShopInstance(ShopInstance value) { shop = value; }
    @Override
    public ShopMenuSnapshot shopcore$menuSnapshot() { return menu; }
    @Override
    public void shopcore$setMenuSnapshot(ShopMenuSnapshot value) { menu = value; }
    @Override
    public ShopOrderBook shopcore$orderBook() { return orders; }
    @Override
    public void shopcore$setOrderBook(ShopOrderBook value) { orders = value; }
    @Override
    public boolean shopcore$openRequested() { return openRequested; }
    @Override
    public void shopcore$setOpenRequested(boolean value) { openRequested = value; }
    @Override
    public void shopcore$runtimeChanged() { changedAndSync(); }
    @Override
    public void shopcore$ordersChanged(List<OrderStateChange> changes) {
        if (host.getLevel() instanceof ServerLevel level) {
            var ended = changes.stream().filter(OrderStateChange::endsOrder)
                    .map(OrderStateChange::orderId).collect(Collectors.toSet());
            if (!ended.isEmpty()) consumers.ordersEnded(population(level), ended);
        }
    }
}

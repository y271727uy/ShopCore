package com.y271727uy.shopcore.text;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.core.consumer.common.ConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.entity.EntityConsumerActor;
import com.y271727uy.shopcore.core.consumer.common.lifecycle.TransientConsumerCleanupService;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutRole;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutSlot;
import com.y271727uy.shopcore.core.consumer.common.layout.ConsumerLayoutTransform;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue.HorizontalQueueContext;
import com.y271727uy.shopcore.core.consumer.common.wait.HorizontalQueue.HorizontalQueueLayout;
import com.y271727uy.shopcore.core.consumer.common.wait.QueueConsumerPhase;
import com.y271727uy.shopcore.core.consumer.minecraft.MinecraftVillagerConsumerActor;
import com.y271727uy.shopcore.core.consumer.minecraft.MinecraftVillagerConsumerMemory;
import com.y271727uy.shopcore.core.consumer.minecraft.MinecraftVillagerConsumerNavigator;
import com.y271727uy.shopcore.core.consumer.minecraft.MinecraftVillagerConsumers;
import com.y271727uy.shopcore.core.market.demand.DemandCategoryKey;
import com.y271727uy.shopcore.core.market.demand.OrderComplexity;
import com.y271727uy.shopcore.core.menu.ListingMenuEntry;
import com.y271727uy.shopcore.core.menu.pool.DemandPoolCatalog;
import com.y271727uy.shopcore.core.order.CustomerProfiles;
import com.y271727uy.shopcore.core.order.OrderLine;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.ShopListing;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.order.generation.FixedIntervalOrderGenerationSchedule;
import com.y271727uy.shopcore.core.order.interaction.OrderDeliveryInteractionResult;
import com.y271727uy.shopcore.core.order.interaction.OrderInteractionService;
import com.y271727uy.shopcore.core.order.interaction.OrderInteractionStatus;
import com.y271727uy.shopcore.core.order.prompt.overhead.OverheadOrderPromptController;
import com.y271727uy.shopcore.core.order.prompt.overhead.CustomerOverheadOrderPromptService;
import com.y271727uy.shopcore.core.order.prompt.screen.PlayerScreenOrderPromptService;
import com.y271727uy.shopcore.core.shop.blockentity.AbstractShopBlockEntity;
import com.y271727uy.shopcore.core.shop.opening.ShopOpeningRuleSet;
import com.y271727uy.shopcore.core.shop.opening.rule.MinListingCountRule;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeBridge;
import com.y271727uy.shopcore.core.shop.runtime.ShopRuntimeTickResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestShopBlockEntity extends AbstractShopBlockEntity {
    private static final String CONSUMERS_TAG = "TestConsumers";
    private static final String DEPARTURE_PARTICLES_SHOWN_TAG = "TestDepartureParticlesShown";
    private static final long OVERHEAD_ORDER_WAIT_TICKS = 20L * 2L;
    private static final long RUNTIME_TICK_INTERVAL = 20L * 5L;
    private static final DemandPoolCatalog DEMAND_POOL_CATALOG = new DemandPoolCatalog();
    private static final ShopOpeningRuleSet OPENING_RULES = ShopOpeningRuleSet.of(
            ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "test_shop"),
            List.of(new MinListingCountRule(1))
    );
    private static final FixedIntervalOrderGenerationSchedule ORDER_GENERATION_SCHEDULE =
            FixedIntervalOrderGenerationSchedule.every(20L * 10L);
    private static final long ORDER_TTL_TICKS = 20L * 60L;

    private final ShopBlockRuntimeBridge runtimeBridge = new ShopBlockRuntimeBridge();
    private final HorizontalQueue queueBehavior = new HorizontalQueue();
    private final MinecraftVillagerConsumerNavigator consumerNavigator = new MinecraftVillagerConsumerNavigator();
    private final MinecraftVillagerConsumerMemory consumerMemory = new MinecraftVillagerConsumerMemory();
    private final OverheadOrderPromptController overheadPromptController =
            new OverheadOrderPromptController(OVERHEAD_ORDER_WAIT_TICKS, Component.literal("\u7b49\u5f85\u4e2d..."));
    private final List<UUID> consumerIds = new ArrayList<>();
    private ShopRuntimeTickResult lastTickResult;
    private boolean resetTransientConsumersAfterLoad;
    private boolean forceRuntimeTick = true;

    public TestShopBlockEntity(BlockPos pos, BlockState state) {
        super(TestShopRegistry.TEST_SHOP_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TestShopBlockEntity entity) {
        if (level.isClientSide) {
            return;
        }
        entity.tickRuntimeIfDue((ServerLevel) level);
        entity.tickTestConsumers((ServerLevel) level, state);
    }

    private void tickRuntimeIfDue(ServerLevel level) {
        if (!forceRuntimeTick && level.getGameTime() % RUNTIME_TICK_INTERVAL != 0L) {
            return;
        }
        forceRuntimeTick = false;
        lastTickResult = tickShopRuntime(runtimeBridge, new ShopBlockRuntimeBridge.ShopBlockRuntimeTickInput(
                DEMAND_POOL_CATALOG,
                OPENING_RULES,
                Optional.of(CustomerProfiles.COMMON),
                Optional.empty(),
                ORDER_GENERATION_SCHEDULE,
                Map.of(),
                true,
                0.0D,
                level.getDayTime(),
                level.getGameTime(),
                ORDER_TTL_TICKS,
                level.random
        ));
    }

    public void toggleOpenRequested() {
        initializeRuntime();
        boolean closing = shopcore$openRequested();
        shopcore$setOpenRequested(!shopcore$openRequested());
        forceRuntimeTick = true;
        if (closing && level instanceof ServerLevel serverLevel) {
            closeTestShopRuntime(serverLevel);
        }
        setChangedAndSyncShopRuntime();
    }

    public MutableComponent statusText() {
        initializeRuntime();
        String diagnostic = lastTickResult == null ? "not_ticked" : lastTickResult.diagnostic().translationKey();
        return Component.translatable("test.shopcore.status",
                shopcore$openRequested(),
                shopcore$shopInstance().isOpen(),
                shopcore$orderBook().activeOrderCount(),
                shopcore$orderBook().orders().size(),
                consumerIds.size(),
                diagnostic
        );
    }

    public MutableComponent activeOrdersText() {
        initializeRuntime();
        if (shopcore$orderBook().activeOrders().isEmpty()) {
            return Component.translatable("test.shopcore.orders.none");
        }
        String orders = shopcore$orderBook().activeOrders().stream()
                .map(this::describeOrder)
                .collect(Collectors.joining(" | "));
        return Component.translatable("test.shopcore.orders.list", orders);
    }

    public MutableComponent recordNextOrderText(ServerPlayer player) {
        initializeRuntime();
        return Component.translatable("test.shopcore.record_order.hint");
    }

    private void initializeRuntime() {
        initializeShopRuntime();
        if (shopcore$menuSnapshot().isEmpty()) {
            shopcore$setMenuSnapshot(shopcore$menuSnapshot().withEntries(List.of(
                    ListingMenuEntry.of(testListing(0, "minecraft:apple", 4)),
                    ListingMenuEntry.of(testListing(1, "minecraft:wheat", 2)),
                    ListingMenuEntry.of(testListing(2, "minecraft:sweet_berries", 3)),
                    ListingMenuEntry.of(testListing(3, "minecraft:potato", 2))
            )));
        }
    }

    private static ShopListing testListing(int slotIndex, String itemId, int unitPrice) {
        return ShopListing.manual(
                slotIndex,
                itemId,
                DemandCategoryKey.BASIC_GOODS,
                OrderComplexity.SINGLE_ITEM,
                unitPrice,
                4,
                ShopListing.UNKNOWN_STOCK
        );
    }

    private void tickTestConsumers(ServerLevel level, BlockState state) {
        resetTransientConsumersAfterLoad(level);
        List<MinecraftVillagerConsumerActor> actors = resolveConsumers(level);
        spawnConsumersForNewOrders(level, state, actors);
        actors = resolveConsumers(level);
        markConsumersWithoutActiveOrdersLeaving(actors);

        queueBehavior.tick(new HorizontalQueueContext(
                horizontalQueueLayout(state),
                new ArrayList<ConsumerActor>(actors),
                consumerNavigator,
                consumerMemory,
                shopcore$shopInstance() != null && shopcore$shopInstance().isOpen(),
                level.getGameTime()
        ));
        overheadPromptController.tickWaitingConsumers(actors, consumerMemory, shopcore$orderBook(), level.getGameTime());
        clearOverheadForLeavingConsumers(actors);
        pruneConsumers(level);
    }

    private void spawnConsumersForNewOrders(ServerLevel level, BlockState state, List<MinecraftVillagerConsumerActor> actors) {
        if (shopcore$shopInstance() == null || !shopcore$shopInstance().isOpen() || shopcore$orderBook() == null) {
            return;
        }
        Set<UUID> representedOrders = actors.stream()
                .map(consumerMemory::orderId)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        HorizontalQueueLayout layout = horizontalQueueLayout(state);
        if (!spawnEntranceClear(layout, actors)) {
            return;
        }
        Vec3 spawnPosition = layout.spawnPosition();
        for (ShopOrder order : shopcore$orderBook().activeOrders()) {
            if (representedOrders.contains(order.orderId())) {
                continue;
            }
            Optional<MinecraftVillagerConsumerActor> spawned = MinecraftVillagerConsumers.spawn(level, spawnPosition);
            if (spawned.isEmpty()) {
                continue;
            }
            MinecraftVillagerConsumerActor actor = spawned.get();
            consumerMemory.setOrderId(actor, order.orderId());
            consumerMemory.setShopPos(actor, worldPosition);
            consumerMemory.setJoinedGameTime(actor, order.createdGameTime());
            consumerIds.add(actor.consumerId());
            representedOrders.add(order.orderId());
            spawnArrivalParticles(level, actor.entity());
            return;
        }
    }

    private boolean spawnEntranceClear(HorizontalQueueLayout layout, List<MinecraftVillagerConsumerActor> actors) {
        return actors.stream()
                .map(layout::toLocal)
                .noneMatch(local -> local.x >= 7.0D);
    }

    public MutableComponent deliverCustomerOrder(ServerPlayer player, Entity customer, ItemStack input) {
        initializeRuntime();
        if (input.isEmpty()) {
            return Component.translatable("test.shopcore.delivery.input_empty");
        }
        EntityConsumerActor actor = new EntityConsumerActor(customer);
        Optional<UUID> orderId = consumerMemory.orderId(actor);
        if (orderId.isEmpty()) {
            return Component.translatable("test.shopcore.delivery.no_order");
        }
        if (consumerMemory.phase(actor) == QueueConsumerPhase.WAITING_FOR_ORDER) {
            return Component.translatable("test.shopcore.delivery.not_recorded");
        }

        OrderDeliveryInteractionResult result = OrderInteractionService.deliverToRecordedCustomer(
                player,
                actor,
                input,
                consumerMemory,
                this,
                (ignoredPlayer, prompt) -> CustomerOverheadOrderPromptService.show(customer, prompt),
                ignoredPlayer -> CustomerOverheadOrderPromptService.clear(customer)
        );
        if (result.status() == OrderInteractionStatus.DELIVERED
                || result.status() == OrderInteractionStatus.COMPLETED) {
            playDeliverySound(player);
        }
        if (result.status() == OrderInteractionStatus.COMPLETED
                && customer.level() instanceof ServerLevel serverLevel) {
            spawnDepartureParticlesOnce(serverLevel, customer);
        }
        setChangedAndSyncShopRuntime();
        return switch (result.status()) {
            case COMPLETED -> Component.translatable("test.shopcore.delivery.completed",
                    describeOrder(result.order()),
                    result.settlementResult().orElseThrow().checkoutResult().totalValue());
            case DELIVERED -> Component.translatable("test.shopcore.delivery.delivered",
                    result.deliveryResult().consumedCount(),
                    describeOrder(result.order()));
            case INPUT_EMPTY -> Component.translatable("test.shopcore.delivery.input_empty");
            case ORDER_NOT_RECORDED -> Component.translatable("test.shopcore.delivery.not_recorded");
            case ORDER_NOT_ACTIVE -> Component.translatable("test.shopcore.delivery.not_active");
            case DELIVERY_NO_MATCH -> Component.translatable("test.shopcore.delivery.no_match");
            case DELIVERY_NOT_ALLOWED -> Component.translatable("test.shopcore.delivery.not_allowed");
            case DELIVERY_UNCHANGED -> Component.translatable("test.shopcore.delivery.unchanged");
            case NO_ORDER, RECORDED -> Component.translatable("test.shopcore.delivery.no_order");
        };
    }

    private void clearOverheadForLeavingConsumers(List<MinecraftVillagerConsumerActor> actors) {
        for (MinecraftVillagerConsumerActor actor : actors) {
            QueueConsumerPhase phase = consumerMemory.phase(actor);
            if (phase == QueueConsumerPhase.LEAVING || phase == QueueConsumerPhase.DONE) {
                if (actor.entity().level() instanceof ServerLevel level) {
                    spawnDepartureParticlesOnce(level, actor.entity());
                }
                overheadPromptController.clear(actor.entity());
            }
        }
    }

    private void markConsumersWithoutActiveOrdersLeaving(List<MinecraftVillagerConsumerActor> actors) {
        if (shopcore$orderBook() == null) {
            return;
        }
        Set<UUID> activeOrders = shopcore$orderBook().activeOrders().stream()
                .map(ShopOrder::orderId)
                .collect(Collectors.toSet());
        for (MinecraftVillagerConsumerActor actor : actors) {
            Optional<UUID> orderId = consumerMemory.orderId(actor);
            if (orderId.isPresent() && !activeOrders.contains(orderId.get())) {
                if (actor.entity().level() instanceof ServerLevel level) {
                    spawnDepartureParticlesOnce(level, actor.entity());
                }
                overheadPromptController.clear(actor.entity());
                consumerMemory.setPhase(actor, QueueConsumerPhase.LEAVING);
            }
        }
    }

    private static void spawnArrivalParticles(ServerLevel level, Entity entity) {
        level.sendParticles(
                ParticleTypes.POOF,
                entity.getX(),
                entity.getY() + 0.25D,
                entity.getZ(),
                15,
                0.1D,
                0.2D,
                0.1D,
                0.01D
        );
    }

    private static void spawnDepartureParticlesOnce(ServerLevel level, Entity entity) {
        CompoundTag data = entity.getPersistentData();
        if (data.getBoolean(DEPARTURE_PARTICLES_SHOWN_TAG)) {
            return;
        }
        data.putBoolean(DEPARTURE_PARTICLES_SHOWN_TAG, true);
        level.sendParticles(
                ParticleTypes.END_ROD,
                entity.getX(),
                entity.getY() + 0.25D,
                entity.getZ(),
                15,
                0.1D,
                0.2D,
                0.1D,
                0.05D
        );
    }

    private static void playDeliverySound(ServerPlayer player) {
        player.serverLevel().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS,
                0.75F,
                1.0F
        );
    }

    private void resetTransientConsumersAfterLoad(ServerLevel level) {
        if (!resetTransientConsumersAfterLoad) {
            return;
        }
        resetTransientConsumersAfterLoad = false;
        discardTransientConsumers(level);
        consumerIds.clear();
        setChangedAndSyncShopRuntime();
    }

    private void closeTestShopRuntime(ServerLevel level) {
        cancelActiveOrders();
        clearNearbyPlayerHud(level);
        discardTransientConsumers(level);
        consumerIds.clear();
    }

    private void discardTransientConsumers(ServerLevel level) {
        TransientConsumerCleanupService.discardForShop(
                level,
                worldPosition,
                consumerIds,
                consumerMemory,
                entity -> entity instanceof Villager,
                32.0D
        );
    }

    private void clearNearbyPlayerHud(ServerLevel level) {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        for (ServerPlayer player : level.players()) {
            if (player.position().distanceToSqr(center) <= 32.0D * 32.0D) {
                PlayerScreenOrderPromptService.clear(player);
            }
        }
    }

    private List<MinecraftVillagerConsumerActor> resolveConsumers(ServerLevel level) {
        List<MinecraftVillagerConsumerActor> actors = new ArrayList<>();
        for (UUID consumerId : consumerIds) {
            Entity entity = level.getEntity(consumerId);
            if (entity instanceof Villager villager && villager.isAlive() && !villager.isRemoved()) {
                actors.add(MinecraftVillagerConsumers.wrap(villager));
            }
        }
        return actors;
    }

    private void pruneConsumers(ServerLevel level) {
        consumerIds.removeIf(consumerId -> {
            Entity entity = level.getEntity(consumerId);
            return !(entity instanceof Villager villager) || !villager.isAlive() || villager.isRemoved();
        });
    }

    private HorizontalQueueLayout horizontalQueueLayout(BlockState state) {
        Direction facing = state.hasProperty(TestShopBlock.FACING) ? state.getValue(TestShopBlock.FACING) : Direction.NORTH;
        ConsumerLayoutTransform transform = new ConsumerLayoutTransform(worldPosition, facing);
        return new HorizontalQueueLayout(
                transform,
                List.of(
                        new ConsumerLayoutSlot(ConsumerLayoutRole.SERVICE, 0, BlockPos.ZERO)
                ),
                List.of(
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 0, BlockPos.ZERO),
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 1, BlockPos.ZERO),
                        new ConsumerLayoutSlot(ConsumerLayoutRole.QUEUE, 2, BlockPos.ZERO)
                ),
                new ConsumerLayoutSlot(ConsumerLayoutRole.EXIT, 0, BlockPos.ZERO),
                facing,
                1.0D,
                0.85D
        );
    }

    private String describeOrder(ShopOrder order) {
        String shortId = order.orderId().toString().substring(0, 8);
        String lines = order.lines().stream()
                .map(this::describeLine)
                .collect(Collectors.joining(", "));
        return "#" + shortId + " " + order.status().name().toLowerCase() + " [" + lines + "]";
    }

    private String describeLine(OrderLine line) {
        return line.requestedCount() + "x " + line.requestedItem().getHoverName().getString()
                + " (" + line.deliveredCount() + "/" + line.requestedCount() + ")";
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadShopRuntime(tag);
        consumerIds.clear();
        ListTag consumers = tag.getList(CONSUMERS_TAG, CompoundTag.TAG_INT_ARRAY);
        for (int i = 0; i < consumers.size(); i++) {
            consumerIds.add(NbtUtils.loadUUID(consumers.get(i)));
        }
        resetTransientConsumersAfterLoad = true;
        forceRuntimeTick = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveShopRuntime(tag);
        ListTag consumers = new ListTag();
        for (UUID consumerId : consumerIds) {
            consumers.add(NbtUtils.createUUID(consumerId));
        }
        tag.put(CONSUMERS_TAG, consumers);
    }
}

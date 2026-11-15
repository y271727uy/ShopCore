package com.y271727uy.shopcore.text;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.api.component.menu.MenuListing;
import com.y271727uy.shopcore.api.component.menu.MenuSource;
import com.y271727uy.shopcore.api.component.order.DeliveryReceipt;
import com.y271727uy.shopcore.api.preset.ShopPresets;
import com.y271727uy.shopcore.api.shop.ShopBlockEntity;
import com.y271727uy.shopcore.api.shop.ShopDefinition;
import com.y271727uy.shopcore.integration.farmerstales.FarmersTalesFoodExpiryCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Reference consumer of the public API: configuration and gameplay feedback only. */
public class TestShopBlockEntity extends ShopBlockEntity {
    private static final String DEPARTURE_PARTICLES_SHOWN_TAG = "TestDepartureParticlesShown";
    private static final ShopDefinition DEFINITION = ShopPresets.horizontalQueue(
            ResourceLocation.fromNamespaceAndPath(ShopcoreMod.MODID, "test_shop"),
            MenuSource.fixed(new MenuListing("minecraft:apple", 4, 4),
                    new MenuListing("minecraft:wheat", 2, 4),
                    new MenuListing("minecraft:sweet_berries", 3, 4),
                    new MenuListing("minecraft:potato", 2, 4)),
            "TestConsumers", "test.shopcore.delivery");

    public TestShopBlockEntity(BlockPos pos, BlockState state) {
        super(TestShopRegistry.TEST_SHOP_BLOCK_ENTITY.get(), pos, state, DEFINITION);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TestShopBlockEntity entity) {
        if (level instanceof ServerLevel serverLevel) entity.shopRuntime().tick(serverLevel);
    }

    @Override
    protected Direction shopFacing() {
        return getBlockState().hasProperty(TestShopBlock.FACING)
                ? getBlockState().getValue(TestShopBlock.FACING) : Direction.NORTH;
    }

    public boolean toggleOpenRequested() {
        return shopRuntime().toggleOpenRequested();
    }

    public MutableComponent statusText() {
        var status = shopRuntime().status();
        return Component.translatable("test.shopcore.status", status.openRequested(), status.open(),
                status.activeOrders(), status.totalOrders(), status.consumers(), status.diagnostic());
    }

    public MutableComponent activeOrdersText() {
        var orders = shopRuntime().activeOrders().stream().map(order ->
                "#" + order.orderId().toString().substring(0, 8) + " " + order.status().name().toLowerCase(java.util.Locale.ROOT)
                + " [" + order.lines().stream().map(line -> line.requestedCount() + "x "
                        + line.requestedItem().getHoverName().getString() + " (" + line.deliveredCount()
                        + "/" + line.requestedCount() + ")").collect(java.util.stream.Collectors.joining(", ")) + "]").toList();
        return orders.isEmpty() ? Component.translatable("test.shopcore.orders.none")
                : Component.translatable("test.shopcore.orders.list", String.join(" | ", orders));
    }

    public MutableComponent recordNextOrderText(ServerPlayer player) {
        shopRuntime().status();
        return Component.translatable("test.shopcore.record_order.hint");
    }

    public MutableComponent deliverCustomerOrder(ServerPlayer player, Entity customer, ItemStack input) {
        return shopRuntime().deliver(player, customer, input).message();
    }

    @Override
    public void arrived(Entity consumer) {
        if (consumer.level() instanceof ServerLevel level) spawnArrivalParticles(level, consumer);
    }

    @Override
    public void departing(Entity consumer) {
        if (consumer.level() instanceof ServerLevel level) spawnDepartureParticlesOnce(level, consumer);
    }

    @Override
    public void delivered(ServerPlayer player, DeliveryReceipt receipt) {
        ItemStack delivered = receipt.inputBefore();
        boolean spoiled = FarmersTalesFoodExpiryCompat.getFreshness(delivered, player.serverLevel().getGameTime())
                == FarmersTalesFoodExpiryCompat.Freshness.ROTTEN;
        TestShopDailySavedData.recordDelivery(player.serverLevel(), delivered, receipt.deliveredCount(), spoiled);
        playDeliverySound(player);
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

    public void forceConsumersToLeaveAfterRemoval(ServerLevel level) {
        shopRuntime().removed(level);
    }
}

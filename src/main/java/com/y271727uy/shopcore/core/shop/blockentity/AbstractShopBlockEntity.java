package com.y271727uy.shopcore.core.shop.blockentity;

import com.y271727uy.shopcore.core.menu.ShopMenuSnapshot;
import com.y271727uy.shopcore.core.order.OrderStatus;
import com.y271727uy.shopcore.core.order.ShopOrder;
import com.y271727uy.shopcore.core.order.book.ShopOrderBook;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeBridge;
import com.y271727uy.shopcore.core.shop.instance.ShopInstance;
import com.y271727uy.shopcore.core.shop.runtime.ShopBlockRuntimeHolder;
import com.y271727uy.shopcore.core.shop.runtime.ShopRuntimePersistence;
import com.y271727uy.shopcore.core.shop.runtime.ShopRuntimeTickResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractShopBlockEntity extends BlockEntity implements ShopBlockRuntimeHolder {
    public static final String SHOP_RUNTIME_TAG = "ShopRuntime";

    private ShopInstance shop;
    private ShopMenuSnapshot menuSnapshot;
    private ShopOrderBook orderBook;
    private boolean openRequested;

    protected AbstractShopBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected final void initializeShopRuntime() {
        ShopRuntimePersistence.initializeIfMissing(this, worldPosition);
    }

    protected final void loadShopRuntime(CompoundTag tag) {
        if (tag.contains(SHOP_RUNTIME_TAG)) {
            ShopRuntimePersistence.load(this, tag.getCompound(SHOP_RUNTIME_TAG), worldPosition);
        }
    }

    protected final void saveShopRuntime(CompoundTag tag) {
        initializeShopRuntime();
        tag.put(SHOP_RUNTIME_TAG, ShopRuntimePersistence.save(this));
    }

    protected final void setChangedAndSyncShopRuntime() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected final ShopRuntimeTickResult tickShopRuntime(
            ShopBlockRuntimeBridge runtimeBridge,
            ShopBlockRuntimeBridge.ShopBlockRuntimeTickInput input
    ) {
        initializeShopRuntime();
        ShopRuntimeTickResult result = Objects.requireNonNull(runtimeBridge, "runtimeBridge").tick(this, input);
        setChanged();
        return result;
    }

    protected final boolean cancelActiveOrders() {
        initializeShopRuntime();
        ShopOrderBook current = shopcore$orderBook();
        List<ShopOrder> updated = new ArrayList<>();
        boolean changed = false;
        for (ShopOrder order : current.orders()) {
            if (order.canReceiveDelivery()) {
                updated.add(order.withStatus(OrderStatus.CANCELLED));
                changed = true;
            } else {
                updated.add(order);
            }
        }
        if (changed) {
            shopcore$setOrderBook(new ShopOrderBook(current.shopId(), updated));
        }
        return changed;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveShopRuntime(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        loadShopRuntime(tag);
    }

    @Override
    public ShopInstance shopcore$shopInstance() {
        return shop;
    }

    @Override
    public void shopcore$setShopInstance(ShopInstance shop) {
        this.shop = shop;
    }

    @Override
    public boolean shopcore$openRequested() {
        return openRequested;
    }

    @Override
    public void shopcore$setOpenRequested(boolean openRequested) {
        this.openRequested = openRequested;
    }

    @Override
    public ShopMenuSnapshot shopcore$menuSnapshot() {
        return menuSnapshot;
    }

    @Override
    public void shopcore$setMenuSnapshot(ShopMenuSnapshot menuSnapshot) {
        this.menuSnapshot = menuSnapshot;
    }

    @Override
    public ShopOrderBook shopcore$orderBook() {
        return orderBook;
    }

    @Override
    public void shopcore$setOrderBook(ShopOrderBook orderBook) {
        this.orderBook = orderBook;
    }
}

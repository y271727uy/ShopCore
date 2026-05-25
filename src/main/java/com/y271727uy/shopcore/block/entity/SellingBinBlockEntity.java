package com.y271727uy.shopcore.block.entity;

import com.y271727uy.shopcore.all.ModBlockEntities;
import com.y271727uy.shopcore.all.ModMenus;
import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.api.economic.ShopcoreCurrency;
import com.y271727uy.shopcore.client.menu.SellingBinMenu;
import com.y271727uy.shopcore.economic.CurrencyDenomination;
import com.y271727uy.shopcore.economic.CurrencyOperationResult;
import com.y271727uy.shopcore.economic.Tax;
import com.y271727uy.shopcore.event.SellingBinEvents;
import com.y271727uy.shopcore.gameplay.sellingbin.SellingBinGroupManager;
import com.y271727uy.shopcore.recipe.SellingBinRecipe;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SellingBinBlockEntity extends BlockEntity implements MenuProvider {

    /** 10秒执行一次配方（可在这里调整） */
    public static final int INTERVAL_TICKS = 10 * 60 * 20;
    private static final float LID_ANIMATION_STEP = 0.1F;

    /** 倒计时：距离下次执行还剩多少 tick */
    private int ticksUntilRun = INTERVAL_TICKS;
    private boolean lidTargetOpen = false;
    private float lastLidOpenProgress = 0.0F;
    private float lidOpenProgress = 0.0F;
    private boolean suppressInventorySync = false;
    @Nullable
    private UUID boundPlayerUuid;
    @Nullable
    private String boundPlayerName;
    private boolean boundTaxExempt;
    private boolean transactionNotificationEnabled;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ticksUntilRun;
                case 1 -> INTERVAL_TICKS;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                ticksUntilRun = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    /**
     * 简单测试用物品槽位。之后你可以改成：把物品“卖出”并给玩家奖励。
     */
    public final ItemStackHandler itemHandler = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!suppressInventorySync && level != null && !level.isClientSide) {
                syncClientState(worldPosition, getBlockState());
            }
        }
    };

    public final LazyOptional<IItemHandler> handlerOptional = LazyOptional.of(() -> itemHandler);

    // Registry of loaded server-side SellingBinBlockEntity instances for debugging/commands
    private static final java.util.Set<SellingBinBlockEntity> LOADED_INSTANCES = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            LOADED_INSTANCES.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        LOADED_INSTANCES.remove(this);
    }

    /**
     * Return a snapshot of currently loaded SellingBinBlockEntity instances on the server.
     */
    public static java.util.Collection<SellingBinBlockEntity> getLoadedInstances() {
        return java.util.List.copyOf(LOADED_INSTANCES);
    }

    public SellingBinBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.SELLING_BIN.get(), pos, state);
    }

    public SellingBinBlockEntity(BlockEntityType<? extends SellingBinBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handlerOptional.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    public void setLidTargetOpen(boolean open) {
        if (this.lidTargetOpen == open) {
            return;
        }

        this.lidTargetOpen = open;
        playLidSound(open);
        syncClientState(worldPosition, getBlockState());
    }

    private void playLidSound(boolean open) {
        if (level == null || level.isClientSide) {
            return;
        }

        level.playSound(
                null,
                worldPosition,
                open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                SoundSource.BLOCKS,
                0.5F,
                level.random.nextFloat() * 0.1F + 0.9F
        );
    }

    private void syncClientState(BlockPos pos, BlockState state) {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean isBound() {
        return boundPlayerUuid != null;
    }

    public boolean isBoundTo(Player player) {
        return isBoundTo(player.getUUID());
    }

    public boolean isBoundTo(UUID playerUuid) {
        return playerUuid.equals(boundPlayerUuid);
    }

    public boolean isTaxExempt() {
        return boundTaxExempt;
    }

    @SuppressWarnings("unused")
    public boolean isTransactionNotificationEnabled() {
        return transactionNotificationEnabled;
    }

    public boolean toggleTransactionNotification() {
        transactionNotificationEnabled = !transactionNotificationEnabled;
        syncClientState(worldPosition, getBlockState());
        return transactionNotificationEnabled;
    }

    @Nullable
    @SuppressWarnings("unused")
    public UUID getBoundPlayerUuid() {
        return boundPlayerUuid;
    }

    @Nullable
    public String getBoundPlayerName() {
        return boundPlayerName;
    }

    public boolean bindTo(Player player, boolean taxExempt) {
        UUID playerUuid = player.getUUID();
        if (boundPlayerUuid != null && !boundPlayerUuid.equals(playerUuid)) {
            return false;
        }

        boundPlayerUuid = playerUuid;
        boundPlayerName = player.getScoreboardName();
        boundTaxExempt = taxExempt;
        syncClientState(worldPosition, getBlockState());
        return true;
    }

    public boolean unbind(Player player) {
        if (!isBoundTo(player)) {
            return false;
        }

        boundPlayerUuid = null;
        boundPlayerName = null;
        boundTaxExempt = false;
        syncClientState(worldPosition, getBlockState());
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("TicksUntilRun", ticksUntilRun);
        if (boundPlayerUuid != null) {
            tag.putUUID("BoundPlayerUuid", boundPlayerUuid);
        }
        if (boundPlayerName != null) {
            tag.putString("BoundPlayerName", boundPlayerName);
        }
        tag.putBoolean("BoundTaxExempt", boundTaxExempt);
        tag.putBoolean("TransactionNotificationEnabled", transactionNotificationEnabled);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("TicksUntilRun", ticksUntilRun);
        tag.putBoolean("LidTargetOpen", lidTargetOpen);
        tag.putFloat("LidOpenProgress", lidOpenProgress);
        tag.putFloat("LastLidOpenProgress", lastLidOpenProgress);
        if (boundPlayerUuid != null) {
            tag.putUUID("BoundPlayerUuid", boundPlayerUuid);
        }
        if (boundPlayerName != null) {
            tag.putString("BoundPlayerName", boundPlayerName);
        }
        tag.putBoolean("BoundTaxExempt", boundTaxExempt);
        tag.putBoolean("TransactionNotificationEnabled", transactionNotificationEnabled);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readInventoryTag(tag);
        ticksUntilRun = tag.contains("TicksUntilRun") ? tag.getInt("TicksUntilRun") : INTERVAL_TICKS;
        this.lidTargetOpen = tag.getBoolean("LidTargetOpen");
        this.lidOpenProgress = tag.getFloat("LidOpenProgress");
        this.lastLidOpenProgress = tag.getFloat("LastLidOpenProgress");
        this.boundPlayerUuid = tag.hasUUID("BoundPlayerUuid") ? tag.getUUID("BoundPlayerUuid") : null;
        this.boundPlayerName = tag.contains("BoundPlayerName") ? tag.getString("BoundPlayerName") : null;
        this.boundTaxExempt = tag.getBoolean("BoundTaxExempt");
        this.transactionNotificationEnabled = tag.getBoolean("TransactionNotificationEnabled");
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            handleUpdateTag(tag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        suppressInventorySync = true;
        try {
            readInventoryTag(tag);
        } finally {
            suppressInventorySync = false;
        }
        ticksUntilRun = tag.contains("TicksUntilRun") ? tag.getInt("TicksUntilRun") : INTERVAL_TICKS;
        this.boundPlayerUuid = tag.hasUUID("BoundPlayerUuid") ? tag.getUUID("BoundPlayerUuid") : null;
        this.boundPlayerName = tag.contains("BoundPlayerName") ? tag.getString("BoundPlayerName") : null;
        this.boundTaxExempt = tag.getBoolean("BoundTaxExempt");
        this.transactionNotificationEnabled = tag.getBoolean("TransactionNotificationEnabled");
    }

    private void readInventoryTag(CompoundTag tag) {
        if (!tag.contains("Inventory")) {
            return;
        }

        // Backward compat: older worlds may have saved only 9 slots.
        // ItemStackHandler NBT format uses {"Size":N, "Items":[{Slot:..}, ...]}
        CompoundTag inv = tag.getCompound("Inventory");
        int savedSize = inv.contains("Size") ? inv.getInt("Size") : itemHandler.getSlots();
        if (savedSize < itemHandler.getSlots()) {
            // Read the old inventory into a temporary handler and copy into our 27-slot handler.
            ItemStackHandler old = new ItemStackHandler(savedSize);
            old.deserializeNBT(inv);
            for (int i = 0; i < Math.min(savedSize, itemHandler.getSlots()); i++) {
                itemHandler.setStackInSlot(i, old.getStackInSlot(i));
            }
        } else {
            itemHandler.deserializeNBT(inv);
        }
    }

    public void dropInventory(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        suppressInventorySync = true;
        try {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                ItemStack stack = itemHandler.getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }

                Block.popResource(level, pos, stack.copy());
                itemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        } finally {
            suppressInventorySync = false;
        }
        setChanged();
    }

    @SuppressWarnings("unused")
    public void tick(Level level, BlockPos pos, BlockState state) {

        float targetProgress = lidTargetOpen ? 1.0F : 0.0F;
        this.lastLidOpenProgress = lidOpenProgress;
        if (lidOpenProgress != targetProgress) {
            if (lidOpenProgress < targetProgress) {
                lidOpenProgress = Math.min(targetProgress, lidOpenProgress + LID_ANIMATION_STEP);
            } else {
                lidOpenProgress = Math.max(targetProgress, lidOpenProgress - LID_ANIMATION_STEP);
            }
        }
        if (level.isClientSide) return;
        if (--ticksUntilRun > 0) {
            setChanged();
            return;
        }

        ticksUntilRun = INTERVAL_TICKS;
        runAllRecipes(level);
        setChanged();
    }

    public float getLidOpenProgress(float partialTick) {
        float targetProgress = lidTargetOpen ? 1.0F : 0.0F;
        if (lidOpenProgress == targetProgress) {
            return targetProgress;
        }
        if (partialTick == 1) {
            return lidOpenProgress;
        }
        float original = Mth.lerp(partialTick, lastLidOpenProgress, lidOpenProgress);

        return original < 0.5f
            ? 4 * original * original * original
            : (float) (1 - Math.pow(-2 * original + 2, 3) / 2);
    }

    public void runAllRecipesBroadcast(Level level) {
        if (level.isClientSide) {
            return;
        }
        runAllRecipes(level);
        setChanged();
        syncClientState(worldPosition, getBlockState());
    }

    private void runAllRecipes(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int slots = itemHandler.getSlots();

        // Snapshot inputs first so outputs inserted during this tick won't affect which slots are processed.
        record Planned(int slot, SellingBinRecipe recipe, int sellCount) {}
        List<Planned> planned = new ArrayList<>();
        boolean marketChanged = false;

        for (int slot = 0; slot < slots; slot++) {
            var stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            var wrapper = new SellingBinRecipe.RecipeInput(List.of(stack));
            var recipeOpt = level.getRecipeManager().getRecipeFor(ModRecipes.SELLING_BIN_RECIPE_TYPE.get(), wrapper, level);
            if (recipeOpt.isEmpty()) continue;

            SellingBinRecipe recipe = recipeOpt.get();
            int inputCount = Math.max(1, recipe.getInputCount());
            int sellCount = stack.getCount() / inputCount;
            if (sellCount <= 0) continue;

            planned.add(new Planned(slot, recipe, sellCount));
        }

        // Execute all planned operations.
        for (Planned p : planned) {
            // Re-check slot still has an item and can consume 1.
            var current = itemHandler.getStackInSlot(p.slot());
            if (current.isEmpty()) continue;
            ItemStack soldStack = current.copy();

            int available = current.getCount();
            int toSell = Math.min(p.sellCount(), available);
            if (toSell <= 0) continue;

            int inputCount = Math.max(1, p.recipe().getInputCount());
            int requiredItems = toSell * inputCount;
            if (requiredItems <= 0 || available < requiredItems) {
                continue;
            }

            // produce outputs: each recipe batch triggers one roll (base/max) and totals are accumulated
            int totalOut = 0;
            for (int k = 0; k < toSell; k++) {
                totalOut += p.recipe().rollOutputCount(level, current);
            }
            if (totalOut <= 0) continue;

            var out = p.recipe().output.copy();
            out.setCount(totalOut);

            if (boundPlayerUuid != null) {
                if (!depositBoundRevenue(level, out)) {
                    continue;
                }

                itemHandler.extractItem(p.slot(), requiredItems, false);
                marketChanged |= SellingBinGroupManager.recordSale(serverLevel, p.recipe(), soldStack, toSell);
                continue;
            }

            // consume all chosen inputs
            itemHandler.extractItem(p.slot(), requiredItems, false);
            marketChanged |= SellingBinGroupManager.recordSale(serverLevel, p.recipe(), soldStack, toSell);

            var remaining = out;
            for (int i = 0; i < slots; i++) {
                remaining = itemHandler.insertItem(i, remaining, false);
                if (remaining.isEmpty()) break;
            }

            if (!remaining.isEmpty()) {
                net.minecraft.world.level.block.Block.popResource(level, worldPosition, remaining);
            }
        }

        if (marketChanged) {
            SellingBinEvents.syncAllPlayers(serverLevel);
        }
    }

    private boolean depositBoundRevenue(Level level, ItemStack outputStack) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return false;
        }
        if (boundPlayerUuid == null) {
            return false;
        }

        var denomination = CurrencyDenomination.fromItemStack(outputStack);
        if (denomination.isEmpty()) {
            return false;
        }

        long grossAmount = denomination.get().totalValue(outputStack.getCount());
        if (grossAmount <= 0L) {
            return false;
        }

        Tax.TaxResult taxResult = Tax.calculate(grossAmount, boundTaxExempt);
        long netAmount = taxResult.netAmount();
        if (netAmount <= 0L) {
            return false;
        }

        Player onlinePlayer = serverLevel.getServer().getPlayerList().getPlayer(boundPlayerUuid);
        CurrencyOperationResult result = onlinePlayer != null
                ? ShopcoreCurrency.increase(onlinePlayer, (double) netAmount)
                : ShopcoreCurrency.increase(boundPlayerUuid, (double) netAmount);

        boolean success = result.success();
        if (!success && onlinePlayer != null) {
            success = ShopcoreCurrency.increase(boundPlayerUuid, (double) netAmount).success();
        }

        if (success && transactionNotificationEnabled && onlinePlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable(
                            "message.shopcore.selling_bin.revenue_notice",
                            Component.literal(Long.toString(netAmount))
                                    .withStyle(net.minecraft.ChatFormatting.GOLD)
                    ).withStyle(net.minecraft.ChatFormatting.GREEN),
                    false
            );
        }

        return success;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.shopcore.selling_bin");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SellingBinMenu(ModMenus.SELLING_BIN.get(), containerId, playerInventory, this, dataAccess);
    }
}


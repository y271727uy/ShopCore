package com.y271727uy.shopcore.block.entity;

import com.y271727uy.shopcore.all.ModBlockEntities;
import com.y271727uy.shopcore.all.ModMenus;
import com.y271727uy.shopcore.all.ModRecipes;
import com.y271727uy.shopcore.client.menu.SellingBinMenu;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
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
import java.util.List;

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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("TicksUntilRun", ticksUntilRun);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("TicksUntilRun", ticksUntilRun);
        tag.putBoolean("LidTargetOpen", lidTargetOpen);
        tag.putFloat("LidOpenProgress", lidOpenProgress);
        tag.putFloat("LastLidOpenProgress", lastLidOpenProgress);
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

    private void runAllRecipes(Level level) {
        int slots = itemHandler.getSlots();

        // Snapshot inputs first so outputs inserted during this tick won't affect which slots are processed.
        record Planned(int slot, SellingBinRecipe recipe, int sellCount) {}
        List<Planned> planned = new java.util.ArrayList<>();

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

            int available = current.getCount();
            int toSell = Math.min(p.sellCount(), available);
            if (toSell <= 0) continue;

            int inputCount = Math.max(1, p.recipe().getInputCount());
            int requiredItems = toSell * inputCount;
            if (requiredItems <= 0 || available < requiredItems) {
                continue;
            }

            // consume all chosen inputs
            itemHandler.extractItem(p.slot(), requiredItems, false);

            // produce outputs: each recipe batch triggers one roll (base/max) and totals are accumulated
            int totalOut = 0;
            for (int k = 0; k < toSell; k++) {
                totalOut += p.recipe().rollOutputCount(level, current);
            }
            if (totalOut <= 0) continue;

            var out = p.recipe().output.copy();
            out.setCount(totalOut);

            var remaining = out;
            for (int i = 0; i < slots; i++) {
                remaining = itemHandler.insertItem(i, remaining, false);
                if (remaining.isEmpty()) break;
            }

            if (!remaining.isEmpty()) {
                net.minecraft.world.level.block.Block.popResource(level, worldPosition, remaining);
            }
        }
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



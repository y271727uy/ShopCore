package com.list.block.entity;

import com.list.all.ModMenus;
import com.list.menu.ForestryGreenhouseMenu;
import com.list.recipe.ForestryGreenhouseRecipe;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated block entity for Forestry Greenhouse.
 *
 * Currently holds no data; provided so the block can evolve independently.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ForestryGreenhouseBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOTS = 12;   // 0-11
    public static final int OUTPUT_SLOTS = 9;   // 12-20
    public static final int TOTAL_SLOTS = INPUT_SLOTS + OUTPUT_SLOTS;

    // 0-11 input slots, 12-20 output slots
    public final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final LazyOptional<IItemHandler> allSidesHandler = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IItemHandler> inputOnlyHandler = LazyOptional.of(() -> new net.minecraftforge.items.wrapper.RangedWrapper(itemHandler, 0, INPUT_SLOTS));
    private final LazyOptional<IItemHandler> outputOnlyHandler = LazyOptional.of(() -> new net.minecraftforge.items.wrapper.RangedWrapper(itemHandler, INPUT_SLOTS, TOTAL_SLOTS));

    // --- processing state ---
    private static final int DEFAULT_MAX_PROGRESS = 200;

    // --- water tank (slot 11 / index 10) ---
    private static final int WATER_PER_BUCKET_MB = 1000;
    // Keeping it simple for now; can be made configurable later.
    private static final int MAX_WATER_MB = 16_000;

    @Getter
    private int waterMb = 0;

    @Getter
    private int progress = 0;

    @Getter
    private int maxProgress = DEFAULT_MAX_PROGRESS;

    @Nullable
    private net.minecraft.resources.ResourceLocation runningRecipe;

    @Nullable
    private ForestryGreenhouseRecipe runningRecipeCache;

    private NonNullList<ItemStack> pendingOutputs = NonNullList.create();

    // --- fuel burning (slot 12 / index 11) ---
    @Getter
    private int burnTime = 0;
    @Getter
    private int burnTimeTotal = 0;

    /** Snapshot of items consumed at recipe start, for rollback if fuel runs out mid-craft. */
    private List<SlotStack> consumedInputsSnapshot = new ArrayList<>();

    /** Water paid at recipe start; refunded if we rollback due to fuel interruption. */
    private int waterPaidForRunningRecipe = 0;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ForestryGreenhouseBlockEntity.this.progress;
                case 1 -> ForestryGreenhouseBlockEntity.this.maxProgress;
                case 2 -> ForestryGreenhouseBlockEntity.this.waterMb;
                case 3 -> ForestryGreenhouseBlockEntity.this.burnTime;
                case 4 -> ForestryGreenhouseBlockEntity.this.burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> ForestryGreenhouseBlockEntity.this.progress = value;
                case 1 -> ForestryGreenhouseBlockEntity.this.maxProgress = value;
                case 2 -> ForestryGreenhouseBlockEntity.this.waterMb = value;
                case 3 -> ForestryGreenhouseBlockEntity.this.burnTime = value;
                case 4 -> ForestryGreenhouseBlockEntity.this.burnTimeTotal = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    private record SlotStack(int slot, ItemStack stack) {
    }

    public ForestryGreenhouseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Component getDisplayName() {
        // Translation key: block.<modid>.forestry_greenhouse
        return Component.translatable("block.list.forestry_greenhouse");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ForestryGreenhouseMenu(ModMenus.FORESTRY_GREENHOUSE.get(), id, inventory, this, data);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        allSidesHandler.invalidate();
        inputOnlyHandler.invalidate();
        outputOnlyHandler.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {

            // If a side is specified, prefer exposing input-only vs output-only to automation.
            // Default/null side => expose full handler (player/menu).
            if (side == null) {
                return allSidesHandler.cast();
            }

            // Common convention: allow insert from sides/top, extract from bottom.
            // (Keeps things usable with hoppers/pipes without extra config.)
            if (side == Direction.DOWN) {
                return outputOnlyHandler.cast();
            }
            return inputOnlyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @SuppressWarnings({"unused", "MethodMayBeStatic"})
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide) return;

        // Slot 11 (index 10): independent water handling.
        handleWaterSlot(level, blockPos);

        // Ensure we have a running recipe if possible.
        if (runningRecipe == null) {
            findRecipe(level);
        }
        if (runningRecipe == null) {
            return;
        }

        if (runningRecipeCache == null) {
            updateCacheRecipe(level);
            if (runningRecipeCache == null) {
                // Recipe removed/reloaded; reset state.
                runningRecipe = null;
                pendingOutputs.clear();
                progress = 0;
                maxProgress = DEFAULT_MAX_PROGRESS;
                setChanged();
                return;
            }
        }

        maxProgress = Math.max(1, runningRecipeCache.time);

        // If this recipe requires fuel, only progress while burning.
        if (runningRecipeCache.fuel) {
            // Only burn fuel when a recipe actually requires it.
            if (isBurning()) {
                burnTime--;
                if (burnTime < 0) burnTime = 0;
                setChanged();
            } else {
                // Try to ignite using the fuel slot.
                tryStartBurning(level, blockPos);
            }

            if (!isBurning()) {
                // Fuel ran out mid-craft: pause until more fuel is provided.
                return;
            }
        }

        // Can we fit outputs? If not, stall.
        if (!canInsertAllPendingOutputs()) {
            return;
        }

        progress++;
        if (progress < maxProgress) {
            setChanged();
            return;
        }

        // Finish cycle: insert outputs and start next recipe.
        insertAllPendingOutputs();
        progress = 0;
        runningRecipe = null;
        runningRecipeCache = null;
        pendingOutputs.clear();
        consumedInputsSnapshot.clear();
        waterPaidForRunningRecipe = 0;
        setChanged();
    }

    private boolean isBurning() {
        return burnTime > 0;
    }

    private void tryStartBurning(Level level, BlockPos pos) {
        if (burnTime > 0) return;

        // Try to consume one fuel item from fuel slot (index 11).
        ItemStack fuelStack = itemHandler.getStackInSlot(ForestryGreenhouseRecipe.FUEL_SLOT_12_INDEX);
        if (fuelStack.isEmpty()) return;
        if (!AbstractFurnaceBlockEntity.isFuel(fuelStack)) return;

        int ticks = AbstractFurnaceBlockEntity.getFuel().getOrDefault(fuelStack.getItem(), 0);
        if (ticks <= 0) return;

        // consume 1 fuel
        ItemStack extracted = itemHandler.extractItem(ForestryGreenhouseRecipe.FUEL_SLOT_12_INDEX, 1, false);
        if (extracted.isEmpty()) return;

        // handle crafting remaining item (like lava bucket -> bucket)
        ItemStack remaining = extracted.getCraftingRemainingItem();
        if (!remaining.isEmpty()) {
            ItemStack leftover = itemHandler.insertItem(ForestryGreenhouseRecipe.FUEL_SLOT_12_INDEX, remaining, false);
            if (!leftover.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, leftover);
            }
        }

        burnTime = ticks;
        burnTimeTotal = ticks;
        setChanged();
    }

    private void handleWaterSlot(Level level, BlockPos blockPos) {
        int slot = ForestryGreenhouseRecipe.WATER_SLOT_11_INDEX;
        ItemStack stack = itemHandler.getStackInSlot(slot);
        if (stack.isEmpty()) return;

        // Only handle vanilla water bucket for now.
        if (stack.is(Items.WATER_BUCKET)) {
            if (waterMb + WATER_PER_BUCKET_MB > MAX_WATER_MB) {
                return; // tank full
            }

            // Consume one water bucket and return empty bucket in the same slot.
            ItemStack extracted = itemHandler.extractItem(slot, 1, false);
            if (extracted.isEmpty()) return;

            waterMb += WATER_PER_BUCKET_MB;

            // Try to put empty bucket back in the same slot; if it can't fit, drop it.
            ItemStack remaining = itemHandler.insertItem(slot, new ItemStack(Items.BUCKET), false);
            if (!remaining.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, remaining);
            }

            level.playSound(null, blockPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
            setChanged();
            return;
        }
    }

    private void findRecipe(Level level) {
        if (runningRecipe != null) return;

        ArrayList<ItemStack> inputs = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inputs.add(itemHandler.getStackInSlot(i));
        }

        level.getRecipeManager().getRecipeFor(
            com.list.all.ModRecipes.FORESTRY_GREENHOUSE_TYPE.get(),
            new ForestryGreenhouseRecipe.RecipeInput(inputs),
            level
        ).ifPresent(recipe -> {
            // Water requirement check (slot 11 water tank). If not enough, keep idle.
            int waterCost = Math.max(0, recipe.water);
            if (waterMb < waterCost) {
                return;
            }

            // If this recipe requires fuel, make sure we can burn.
            // We allow starting if currently burning OR we have at least one valid fuel item ready.
            if (recipe.fuel && !isBurning()) {
                ItemStack fuelStack = itemHandler.getStackInSlot(ForestryGreenhouseRecipe.FUEL_SLOT_12_INDEX);
                if (fuelStack.isEmpty() || !AbstractFurnaceBlockEntity.isFuel(fuelStack)) {
                    return;
                }
            }

            runningRecipe = recipe.getId();
            runningRecipeCache = recipe;
            progress = 0;
            maxProgress = Math.max(1, recipe.time);
            setChanged();

            // Pay water and consume items at start (commit point).
            if (waterCost > 0) {
                waterMb -= waterCost;
                if (waterMb < 0) waterMb = 0;
                waterPaidForRunningRecipe = waterCost;
            }

            // Take a snapshot of what we consumed so we can rollback if fuel runs out.
            captureAndConsumeRecipeInputs();
            computePendingOutputs(level);

            // Ensure we are burning for fuel-required recipes.
            if (recipe.fuel && !isBurning()) {
                tryStartBurning(level, this.worldPosition);
                // If still not burning, remain paused until fuel becomes available.
            }
        });
    }

    private void captureAndConsumeRecipeInputs() {
        consumedInputsSnapshot.clear();

        // Capture & consume basic inputs (slots 0-8)
        ForestryGreenhouseRecipe.RecipeInput inputView = new ForestryGreenhouseRecipe.RecipeInput(getCurrentInputs());
        runningRecipeCache.computeBasicConsumptionPlan(inputView).ifPresent(plan -> {
            for (java.util.Map.Entry<Integer, Integer> e : plan.entrySet()) {
                int slot = e.getKey();
                int amount = e.getValue();
                if (amount <= 0) continue;
                ItemStack extracted = itemHandler.extractItem(slot, amount, false);
                if (!extracted.isEmpty()) {
                    consumedInputsSnapshot.add(new SlotStack(slot, extracted));
                }
            }
        });

        // Capture & consume trench slot 10 (index 9) if required
        if (runningRecipeCache.trench10 != null) {
            int amount = Math.max(1, runningRecipeCache.trench10.count());
            int slot = ForestryGreenhouseRecipe.TRENCH_SLOT_10_INDEX;
            ItemStack extracted = itemHandler.extractItem(slot, amount, false);
            if (!extracted.isEmpty()) {
                consumedInputsSnapshot.add(new SlotStack(slot, extracted));
            }
        }

        // Slot 11 water slot ignored; Slot 12 fuel slot ignored.

        setChanged();
    }

    // Note: rollbackRunningRecipe removed; fuel starvation now pauses the running recipe.

    private void updateCacheRecipe(Level level) {
        if (runningRecipe == null) {
            runningRecipeCache = null;
            return;
        }
        level.getRecipeManager()
            .byKey(runningRecipe)
            .filter(r -> r instanceof ForestryGreenhouseRecipe)
            .ifPresentOrElse(
                r -> runningRecipeCache = (ForestryGreenhouseRecipe) r,
                () -> runningRecipeCache = null
            );
    }

    private void consumeRecipeInputs() {
        if (runningRecipeCache == null) return;

        // Slots 0-8: use shapeless consumption plan
        ForestryGreenhouseRecipe.RecipeInput inputView = new ForestryGreenhouseRecipe.RecipeInput(getCurrentInputs());
        runningRecipeCache.computeBasicConsumptionPlan(inputView).ifPresent(plan -> {
            for (java.util.Map.Entry<Integer, Integer> e : plan.entrySet()) {
                int slot = e.getKey();
                int amount = e.getValue();
                if (amount > 0) {
                    itemHandler.extractItem(slot, amount, false);
                }
            }
        });

        // Slot 10 (index 9)
        if (runningRecipeCache.trench10 != null) {
            itemHandler.extractItem(
                ForestryGreenhouseRecipe.TRENCH_SLOT_10_INDEX,
                Math.max(1, runningRecipeCache.trench10.count()),
                false
            );
        }

        // Slot 11 (index 10) : water slot, ignored

        // Slot 12 (index 11) : fuel slot, ignored by recipe consumption
        setChanged();
    }

    private List<ItemStack> getCurrentInputs() {
        ArrayList<ItemStack> inputs = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            inputs.add(itemHandler.getStackInSlot(i));
        }
        return inputs;
    }

    private void computePendingOutputs(Level level) {
        pendingOutputs.clear();
        if (runningRecipeCache == null) return;
        // Keep same rolling behavior as Hybridizer: per-item chance, and merge identical outputs.
        for (com.list.recipe.ChancedItemStack out : runningRecipeCache.outputs) {
            ItemStack prototype = out.itemStack();
            int count = prototype.getCount();
            float chance = out.chance();
            for (int i = 0; i < count; i++) {
                if (chance < 1.0f && level.random.nextFloat() > chance) {
                    continue;
                }
                boolean merged = false;
                for (ItemStack existing : pendingOutputs) {
                    if (ItemStack.isSameItemSameTags(existing, prototype)
                        && existing.getCount() < existing.getMaxStackSize()) {
                        existing.grow(1);
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    pendingOutputs.add(prototype.copyWithCount(1));
                }
            }
        }
        setChanged();
    }

    private boolean canInsertAllPendingOutputs() {
        if (pendingOutputs.isEmpty()) return true;

        // Simulate insertion for each output into output range.
        for (ItemStack stack : pendingOutputs) {
            if (stack.isEmpty()) continue;
            ItemStack remaining = stack.copy();
            for (int slot = INPUT_SLOTS; slot < TOTAL_SLOTS; slot++) {
                remaining = itemHandler.insertItem(slot, remaining, true);
                if (remaining.isEmpty()) break;
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void insertAllPendingOutputs() {
        if (pendingOutputs.isEmpty()) return;

        for (ItemStack stack : pendingOutputs) {
            if (stack.isEmpty()) continue;
            ItemStack remaining = stack.copy();
            for (int slot = INPUT_SLOTS; slot < TOTAL_SLOTS; slot++) {
                remaining = itemHandler.insertItem(slot, remaining, false);
                if (remaining.isEmpty()) break;
            }
            if (!remaining.isEmpty() && level != null) {
                // As a last resort, drop to world to avoid voiding items.
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, remaining);
            }
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
		tag.put("Inventory", itemHandler.serializeNBT());
    tag.putInt("Progress", progress);
    tag.putInt("MaxProgress", maxProgress);
    tag.putInt("WaterMb", waterMb);
    tag.putInt("BurnTime", burnTime);
    tag.putInt("BurnTimeTotal", burnTimeTotal);
    tag.putInt("WaterPaid", waterPaidForRunningRecipe);

    if (!consumedInputsSnapshot.isEmpty()) {
        ListTag list = new ListTag();
        for (SlotStack ss : consumedInputsSnapshot) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", ss.slot);
            entry.put("Stack", ss.stack.save(new CompoundTag()));
            list.add(entry);
        }
        tag.put("ConsumedInputs", list);
    }
    if (runningRecipe != null) {
      tag.putString("RunningRecipe", runningRecipe.toString());
    }
    if (!pendingOutputs.isEmpty()) {
      ListTag list = new ListTag();
      for (ItemStack stack : pendingOutputs) {
        CompoundTag s = new CompoundTag();
        stack.save(s);
        list.add(s);
      }
      tag.put("PendingOutputs", list);
    }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
		if (tag.contains("Inventory")) {
			itemHandler.deserializeNBT(tag.getCompound("Inventory"));
		}
    progress = tag.getInt("Progress");
    maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_MAX_PROGRESS;
    waterMb = tag.getInt("WaterMb");
    burnTime = tag.getInt("BurnTime");
    burnTimeTotal = tag.getInt("BurnTimeTotal");
    waterPaidForRunningRecipe = tag.getInt("WaterPaid");
    runningRecipe = tag.contains("RunningRecipe") ? net.minecraft.resources.ResourceLocation.parse(tag.getString("RunningRecipe")) : null;
    runningRecipeCache = null;
    pendingOutputs.clear();
    consumedInputsSnapshot.clear();
    if (tag.contains("ConsumedInputs")) {
        ListTag list = tag.getList("ConsumedInputs", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getInt("Slot");
            ItemStack stack = ItemStack.of(entry.getCompound("Stack"));
            if (!stack.isEmpty()) {
                consumedInputsSnapshot.add(new SlotStack(slot, stack));
            }
        }
    }
    if (tag.contains("PendingOutputs")) {
      ListTag list = tag.getList("PendingOutputs", net.minecraft.nbt.Tag.TAG_COMPOUND);
      for (int i = 0; i < list.size(); i++) {
        pendingOutputs.add(ItemStack.of(list.getCompound(i)));
      }
    }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}


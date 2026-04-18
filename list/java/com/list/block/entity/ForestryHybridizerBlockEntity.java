package com.list.block.entity;

import com.list.recipe.ForestryHybridizerRecipe;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ForestryHybridizerBlockEntity extends MulitblockBlockEntity implements MenuProvider {
    private ResourceLocation runningRecipe = null;
    @Getter
    @Nullable
    private ForestryHybridizerRecipe runningRecipeCache = null;
    @Getter
    private int progress = 0;
    @Getter
    private int maxProgress = 200;
    @Getter
    private boolean isFormed = true;
    // cached actual outputs (randomized once when recipe starts)
    @Getter
    private final List<ItemStack> pendingOutputs = new ArrayList<>();

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    // inputs 0-2, outputs 3-4
    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!isFormed) return;
            findRecipe();
        }
    };

    public final LazyOptional<IItemHandler> handlerOptional = LazyOptional.of(() -> itemHandler);

    public ForestryHybridizerBlockEntity(BlockEntityType<ForestryHybridizerBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return handlerOptional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.list.forestry_hybridizer");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new com.list.menu.ForestryHybridizerMenu(com.list.all.ModMenus.FORESTRY_HYBRIDIZER.get(), id, inventory, this, dataAccess);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", itemHandler.serializeNBT());
        if (runningRecipe != null) {
            tag.putString("RunningRecipe", runningRecipe.toString());
        }
        ListTag output = new ListTag();
        for (ItemStack stack : pendingOutputs) {
            output.add(stack.save(new CompoundTag()));
        }
        tag.put("PendingOutputs", output);
        tag.putInt("Progress", progress);
        tag.putBoolean("IsFormed", isFormed);
    }

    @SuppressWarnings("removal")
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        }
        if (tag.contains("RunningRecipe")) {
            runningRecipe = new ResourceLocation(tag.getString("RunningRecipe"));
            updateCacheRecipe();
        } else {
            tag.remove("RunningRecipe");
        }
        if (tag.contains("PendingOutputs")) {
            ListTag output = tag.getList("PendingOutputs", ListTag.TAG_COMPOUND);
            pendingOutputs.clear();
            for (int i = 0; i < output.size(); i++) {
                ItemStack itemStack = ItemStack.of(output.getCompound(i));
                pendingOutputs.add(itemStack);
            }
        }
        progress = tag.getInt("Progress");
        isFormed = tag.getBoolean("IsFormed");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @SuppressWarnings({"unused","MethodMayBeStatic"})
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        // ForestryHybridizer is a single-block machine.
        isFormed = true;

        // Ensure we have a running recipe if possible.
        if (runningRecipe == null) {
            findRecipe();
        }
        if (runningRecipe == null) {
            return;
        }

        if (runningRecipeCache == null) {
            updateCacheRecipe();
            if (runningRecipeCache == null) {
                runningRecipe = null;
                pendingOutputs.clear();
                progress = 0;
                maxProgress = 200;
                setChanged();
                return;
            }
        }

            // keep maxProgress in sync
            maxProgress = Math.max(1, runningRecipeCache.time);

            // if pendingOutputs somehow missing (e.g. old save), recompute deterministically now
            if (pendingOutputs.isEmpty()) {
                computePendingOutputs(level);
            }

            // Completion check: only finish if outputs can fit.
            if (progress >= maxProgress) {
                if (!canInsertAllPendingOutputs()) {
                    return;
                }
                insertAllPendingOutputs();

                runningRecipe = null;
                runningRecipeCache = null;
                pendingOutputs.clear();
                progress = 0;
                maxProgress = 200;
                setChanged();
                findRecipe();
                return;
            }

            // While running, if output is blocked we can either pause or still progress.
            // Pause to avoid "finish but can't output" jitter.
            if (!canInsertAllPendingOutputs()) {
                return;
            }


        progress++;
        setChanged();
    }

    private void computePendingOutputs(Level level) {
        pendingOutputs.clear();
        if (runningRecipeCache == null) return;

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
    }

    private void findRecipe() {
        if (runningRecipe != null) return;
        ArrayList<ItemStack> inputs = new ArrayList<>();
        inputs.add(itemHandler.getStackInSlot(0));
        inputs.add(itemHandler.getStackInSlot(1));
        inputs.add(itemHandler.getStackInSlot(2));

        if (level == null) return;
        level.getRecipeManager().getRecipeFor(
            com.list.all.ModRecipes.FORESTRY_HYBRIDIZER_TYPE.get(),
            new ForestryHybridizerRecipe.RecipeInput(inputs),
            level
        ).ifPresent(recipe -> {
            runningRecipe = recipe.getId();
            runningRecipeCache = recipe;
            progress = 0;
            maxProgress = Math.max(1, recipe.time);
            setChanged();
            consumeRecipeInputs();
            computePendingOutputs(level);
        });
    }

    private void consumeRecipeInputs() {
        if (runningRecipeCache == null) return;
        // Consume only provided inputs. Missing inputs mean "no requirement" and are not consumed.
        for (int i = 0; i < runningRecipeCache.inputs.size(); i++) {
            int count = Math.max(1, runningRecipeCache.inputs.get(i).count());
            itemHandler.extractItem(i, count, false);
        }
    }

    private void updateCacheRecipe() {
        if (level != null && runningRecipe != null) {
            level.getRecipeManager()
                .byKey(runningRecipe)
                .filter(r -> r instanceof ForestryHybridizerRecipe)
                .ifPresent(r -> runningRecipeCache = (ForestryHybridizerRecipe) r);
            if (runningRecipeCache != null) {
                maxProgress = Math.max(1, runningRecipeCache.time);
            }
        }
    }

    private boolean canInsertAllPendingOutputs() {
        if (pendingOutputs.isEmpty()) {
            return true;
        }

        List<ItemStack> simulatedOutputs = new ArrayList<>(2);
        for (int slot = 3; slot < 5; slot++) {
            simulatedOutputs.add(itemHandler.getStackInSlot(slot).copy());
        }

        for (ItemStack output : pendingOutputs) {
            if (output.isEmpty()) {
                continue;
            }
            ItemStack remaining = output.copy();
            for (int slot = 0; slot < simulatedOutputs.size(); slot++) {
                remaining = insertIntoSimulatedSlot(simulatedOutputs, slot, remaining);
                if (remaining.isEmpty()) {
                    break;
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void insertAllPendingOutputs() {
        for (ItemStack output : pendingOutputs) {
            ItemStack remaining = output.copy();
            for (int i = 3; i < 5; i++) {
                remaining = itemHandler.insertItem(i, remaining.copy(), false);
                if (remaining.isEmpty()) break;
            }
        }
    }

    private ItemStack insertIntoSimulatedSlot(List<ItemStack> simulatedOutputs, int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = simulatedOutputs.get(slot);
        int slotLimit = itemHandler.getSlotLimit(slot + 3);
        int stackLimit = Math.min(stack.getMaxStackSize(), slotLimit);
        if (stackLimit <= 0) {
            return stack;
        }

        if (slotStack.isEmpty()) {
            int toMove = Math.min(stack.getCount(), stackLimit);
            simulatedOutputs.set(slot, stack.copyWithCount(toMove));
            ItemStack remaining = stack.copy();
            remaining.shrink(toMove);
            return remaining;
        }

        if (!canStacksMerge(slotStack, stack)) {
            return stack;
        }

        int maxCount = Math.min(slotStack.getMaxStackSize(), slotLimit);
        int space = maxCount - slotStack.getCount();
        if (space <= 0) {
            return stack;
        }

        int toMove = Math.min(stack.getCount(), space);
        slotStack.grow(toMove);
        ItemStack remaining = stack.copy();
        remaining.shrink(toMove);
        return remaining;
    }

    private static boolean canStacksMerge(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameTags(first, second);
    }

    @Nullable
    @Override
    public List<Definition> createPattern(String patternName) {
        // No multiblock patterns.
        return null;
    }
}

//我觉得copilot还行，反正我也懒得自己写方块实体了
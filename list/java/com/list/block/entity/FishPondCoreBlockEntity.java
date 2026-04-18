package com.list.block.entity;

import com.list.all.ModMenus;
import com.list.all.ModRecipes;
import com.list.menu.FishPondMenu;
import com.list.recipe.ChancedItemStack;
import com.list.recipe.FishPondRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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
import java.util.Optional;

@SuppressWarnings("removal")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FishPondCoreBlockEntity extends MulitblockBlockEntity implements MenuProvider {
    private ResourceLocation runningRecipe = null;
    @Getter
    @Nullable
    private FishPondRecipe runningRecipeCache = null;
    @Getter
    private int progress = 0;
    private int multiblockCheckCooldown = 20 * 20;
    @Getter
    private boolean isFormed = false;
    @Getter
    private boolean isLava = false;
    // cached actual outputs (randomized once when recipe starts)
    @Getter
    private final List<ItemStack> pendingOutputs = new ArrayList<>();
    //这里

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> runningRecipeCache != null ? runningRecipeCache.time : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> {}
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    // 0-8 input, 9-11 output
    public final ItemStackHandler itemHandler = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!isFormed) return;
            findRecipe();
        }
    };

    public final LazyOptional<IItemHandler> handlerOptional = LazyOptional.of(() -> itemHandler);

    public FishPondCoreBlockEntity(BlockEntityType<FishPondCoreBlockEntity> type, BlockPos pos, BlockState state) {
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
        return Component.translatable("block.list.fishpond_core");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FishPondMenu(ModMenus.FISH_POND.get(), id, inventory, this, dataAccess);
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
        tag.putBoolean("IsLava", isLava);
    }

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
        isLava = tag.getBoolean("IsLava");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (multiblockCheckCooldown-- <= 0) {
            if (multiblockFormed("water")) {
                isFormed = true;
                isLava = false;
                findRecipe();
            } else if (multiblockFormed("lava")) {
                isFormed = true;
                isLava = true;
                findRecipe();
            } else {
                isFormed = false;
            }
            multiblockCheckCooldown = 20 * 20;
        }
        if (!isFormed) {
            return;
        }
        if (runningRecipe != null) {
            if(runningRecipeCache == null) {
                updateCacheRecipe();
                if (runningRecipeCache == null) {
                    // invalid recipe, clear state
                    runningRecipe = null;
                    pendingOutputs.clear();
                    progress = 0;
                    setChanged();
                    return;
                }
            }
            if (progress >= runningRecipeCache.time) {
                // Preview: check each pending output can fit into output slots
                for (ItemStack output : pendingOutputs) {
                    ItemStack remaining = output.copy();
                    for (int i = 9; ; i++) {
                        remaining = itemHandler.insertItem(i, remaining.copy(), true);
                        if (remaining.isEmpty()) {
                            break;
                        }
                        if (i == 11) {
                            return;
                        }
                    }
                }

                // Actually insert pending outputs (deterministic now)
                for (ItemStack output : pendingOutputs) {
                    ItemStack remaining = output.copy();
                    for (int i = 9; i < 12; i++) {
                        remaining = itemHandler.insertItem(i, remaining.copy(), false);
                        if (remaining.isEmpty()) {
                            break;
                        }
                    }
                }


                // 播放水桶倒水的声音
                if (!isLava) { // 只在水模式下播放水声
                    level.playSound(null, blockPos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
                } else { // 在岩浆模式下播放岩浆声
                    level.playSound(null, blockPos, net.minecraft.sounds.SoundEvents.BUCKET_EMPTY_LAVA,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.0F);
                }
                // clear recipe state
                runningRecipe = null;
                runningRecipeCache = null;
                pendingOutputs.clear();
                progress = 0;
                findRecipe();
            }
            progress++;
            setChanged();
        }
    }

    private void computePendingOutputs(Level level) {
        pendingOutputs.clear();
        if (runningRecipeCache == null) return;

        for (ChancedItemStack out : runningRecipeCache.results) {
            ItemStack prototype = out.itemStack();
            int count = prototype.getCount();
            float chance = out.chance();

            for (int i = 0; i < count; i++) {
                if (chance < 1.0f) {
                    if (level.random.nextFloat() > chance) {
                        continue;
                    }
                }
                // try to merge into existing pending stacks
                boolean merged = false;
                for (ItemStack s : pendingOutputs) {
                    if (ItemStack.isSameItemSameTags(s, prototype) && s.getCount() < s.getMaxStackSize()) {
                        s.grow(1);
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    ItemStack single = prototype.copyWithCount(1);
                    pendingOutputs.add(single);
                }
            }
        }
    }

    private void findRecipe() {
        if(runningRecipe != null) {
            return;
        }
        ArrayList<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            inputs.add(itemHandler.getStackInSlot(i));
        }
        level.getRecipeManager().getRecipeFor(
            ModRecipes.FISH_POND_RECIPE_TYPE.get(),
            new FishPondRecipe.RecipeInput(inputs, isLava),
            level
        ).ifPresent(recipe -> {
            runningRecipe = recipe.getId();
            runningRecipeCache = recipe;
            progress = 0;
            setChanged();
            consumeRecipeInputs();
            computePendingOutputs(level);
        });
    }

    private void consumeRecipeInputs() {
        if (runningRecipe == null) {
            return;
        }
        for (Ingredient ingredient : runningRecipeCache.ingredients) {
            for (int i = 0; i < 9; i++) {
                ItemStack stackInSlot = itemHandler.getStackInSlot(i);
                if (ingredient.test(stackInSlot)) {
                    itemHandler.extractItem(i, 1, false);
                    break;
                }
            }
        }
    }

    private void updateCacheRecipe() {
        if (level != null && runningRecipe != null) {
            level.getRecipeManager()
                .byKey(runningRecipe)
                .ifPresent(recipe -> runningRecipeCache = (FishPondRecipe) recipe);
        }
    }

    @Nullable
    @Override
    public List<Definition> createPattern(String patternName) {
        List<Definition> commonPattern = new ArrayList<>();
        commonPattern.add(Definition.of(new BlockPos(-2, -2, 0), Blocks.CHISELED_STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -2, 0), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(0, -2, 0), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(1, -2, 0), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(2, -2, 0), Blocks.CHISELED_STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, -1, 0), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -1, 0), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(0, -1, 0), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, -1, 0), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -1, 0), Blocks.STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, 0, 0), Blocks.STONE_BRICK_SLAB));
        commonPattern.add(Definition.of(new BlockPos(-1, 0, 0), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, 0, 0), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, 0, 0), Blocks.STONE_BRICK_SLAB));

        commonPattern.add(Definition.of(new BlockPos(-2, -2, -1), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -2, -1), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(0, -2, -1), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, -2, -1), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -2, -1), Blocks.STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, -1, -1), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -1, -1), Blocks.SMOOTH_STONE));

        commonPattern.add(Definition.of(new BlockPos(-2, 0, -1), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, 0, -1), Blocks.SMOOTH_STONE));

        commonPattern.add(Definition.of(new BlockPos(-2, -2, -2), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -2, -2), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(0, -2, -2), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, -2, -2), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -2, -2), Blocks.STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, -1, -2), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -1, -2), Blocks.SMOOTH_STONE));

        commonPattern.add(Definition.of(new BlockPos(-2, 0, -2), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, 0, -2), Blocks.SMOOTH_STONE));

        commonPattern.add(Definition.of(new BlockPos(-2, -2, -3), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -2, -3), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(0, -2, -3), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, -2, -3), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -2, -3), Blocks.STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, -1, -3), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -1, -3), Blocks.SMOOTH_STONE));

        commonPattern.add(Definition.of(new BlockPos(-2, 0, -3), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, 0, -3), Blocks.SMOOTH_STONE));

        commonPattern.add(Definition.of(new BlockPos(-2, -2, -4), Blocks.CHISELED_STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -2, -4), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(0, -2, -4), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(1, -2, -4), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(2, -2, -4), Blocks.CHISELED_STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, -1, -4), Blocks.STONE_BRICKS));
        commonPattern.add(Definition.of(new BlockPos(-1, -1, -4), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(0, -1, -4), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, -1, -4), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, -1, -4), Blocks.STONE_BRICKS));

        commonPattern.add(Definition.of(new BlockPos(-2, 0, -4), Blocks.STONE_BRICK_SLAB));
        commonPattern.add(Definition.of(new BlockPos(-1, 0, -4), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(0, 0, -4), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(1, 0, -4), Blocks.SMOOTH_STONE));
        commonPattern.add(Definition.of(new BlockPos(2, 0, -4), Blocks.STONE_BRICK_SLAB));
        switch (patternName) {
            case "water" -> {
                List<Definition> waterPattern = new ArrayList<>(commonPattern);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z >= -3; z--) {
                        waterPattern.add(Definition.of(new BlockPos(x, 0, z), Blocks.WATER));
                    }
                }
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z >= -3; z--) {
                        waterPattern.add(Definition.of(new BlockPos(x, -1, z), Blocks.SAND));
                    }
                }
                return waterPattern;
            }
            case "lava" -> {
                List<Definition> lavaPattern = new ArrayList<>(commonPattern);
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z >= -3; z--) {
                        lavaPattern.add(Definition.of(new BlockPos(x, 0, z), Blocks.LAVA));
                    }
                }
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z >= -3; z--) {
                        lavaPattern.add(Definition.of(new BlockPos(x, -1, z), Blocks.SOUL_SAND));
                    }
                }
                return lavaPattern;
            }
            default -> {
                return null;
            }
        }
    }
}

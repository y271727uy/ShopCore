package com.y271727uy.shopcore.client.menu;

import com.y271727uy.shopcore.all.ModBlock;
import com.y271727uy.shopcore.block.entity.SellingBinBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SellingBinMenu extends AbstractContainerMenu {
    private final SellingBinBlockEntity blockEntity;
    private final ContainerData data;

    public SellingBinMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(menuType, containerId, inventory, resolveBlockEntity(inventory, buf), new SimpleContainerData(2));
    }

    private static BlockEntity resolveBlockEntity(Inventory inventory, FriendlyByteBuf buf) {
        var level = inventory.player.level();
        return level.getBlockEntity(buf.readBlockPos());
    }

    public SellingBinMenu(MenuType<?> menuType, int containerId, Inventory inventory, BlockEntity be, ContainerData data) {
        super(menuType, containerId);
        this.blockEntity = (SellingBinBlockEntity) be;
        this.data = data;

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        // 3x9 inventory (0-26)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9;
                this.addSlot(new SlotItemHandler(blockEntity.itemHandler, slot, 8 + col * 18, 18 + row * 18));
            }
        }

        addDataSlots(data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private static final int TE_INVENTORY_SLOT_COUNT = 27;

    public int getTicksUntilRun() {
        return data.get(0);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        final ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // player -> TE
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // TE -> player
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        var level = blockEntity.getLevel();
        return level != null && stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlock.SELLING_BIN.get());
    }

    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
        var playerLevel = player.level();
        var blockLevel = blockEntity.getLevel();
        if (blockLevel != null && playerLevel == blockLevel && !blockLevel.isClientSide) {
            blockEntity.setLidTargetOpen(false);
        }
    }
}


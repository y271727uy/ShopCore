package com.list.menu;

import com.list.all.ModBlocks;
import com.list.block.entity.ForestryGreenhouseBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * Minimal menu for Forestry Greenhouse.
 *
 * Currently provides only the player inventory/hotbar; add TE slots when gameplay is implemented.
 */
public class ForestryGreenhouseMenu extends AbstractContainerMenu {
    private final Level level;
    private final ForestryGreenhouseBlockEntity blockEntity;
    private final ContainerData data;

    public ForestryGreenhouseMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(menuType, containerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(5));
    }

    public ForestryGreenhouseMenu(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, BlockEntity be, ContainerData data) {
        super(menuType, containerId);
        this.level = inventory.player.level();
        this.blockEntity = be instanceof ForestryGreenhouseBlockEntity fg ? fg : null;
        this.data = data;

        // Use the BE's handler when possible; otherwise use a safe dummy handler so the menu doesn't crash.
        // Total slots: 12 inputs + 9 outputs = 21
        ItemStackHandler handler = this.blockEntity != null ? this.blockEntity.itemHandler : new ItemStackHandler(21);

        // TE input slots (0-8) at the provided coordinates
        this.addSlot(new SlotItemHandler(handler, 0, 14, 29));
        this.addSlot(new SlotItemHandler(handler, 1, 14, 47));
        this.addSlot(new SlotItemHandler(handler, 2, 14, 65));

        this.addSlot(new SlotItemHandler(handler, 3, 32, 29));
        this.addSlot(new SlotItemHandler(handler, 4, 32, 47));
        this.addSlot(new SlotItemHandler(handler, 5, 32, 65));

        this.addSlot(new SlotItemHandler(handler, 6, 50, 29));
        this.addSlot(new SlotItemHandler(handler, 7, 50, 47));
        this.addSlot(new SlotItemHandler(handler, 8, 50, 65));

        // extra independent input slots (9-11)
        // 9  = trench slot (free placement)
        this.addSlot(new SlotItemHandler(handler, 9, 50, 86));
        // 10 = water slot (bucket handling)
        this.addSlot(new SlotItemHandler(handler, 10, 70, 86));
        // 11 = fuel slot (only accepts furnace fuels)
        this.addSlot(new SlotItemHandler(handler, 11, 90, 86) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel(stack);
            }
        });

        // output slots (12-20)
        this.addSlot(new SlotItemHandler(handler, 12, 115, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(handler, 13, 133, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(handler, 14, 151, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(handler, 15, 115, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(handler, 16, 133, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(handler, 17, 151, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(handler, 18, 115, 65) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(handler, 19, 133, 65) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new SlotItemHandler(handler, 20, 151, 65) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);

        addDataSlots(data);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 123 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 181));
        }
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        // No TE slots yet, so nothing special to shift-click.
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) {
            return false;
        }
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.FORESTRY_GREENHOUSE.get());
    }

    public int getProgress(int width) {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        if (maxProgress <= 0) {
            // BlockEntity currently doesn't provide maxProgress; make the bar usable for now.
            maxProgress = 200;
        }
        return progress > 0 ? (int) (progress * width / (float) maxProgress) : 0;
    }

    public int getWaterMb() {
        int waterMb = this.data.get(2);
        return Math.max(waterMb, 0);
    }

    public int getBurnTime(int height) {
        int burnTime = this.data.get(3);
        int maxBurnTime = this.data.get(4);
        if (maxBurnTime <= 0) {
            maxBurnTime = 300;
        }
        return burnTime > 0 ? (int) (burnTime * height / (float) maxBurnTime) : 0;
    }
}


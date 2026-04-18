package com.list.all;

import com.list.block.entity.FishPondCoreBlockEntity;
import com.list.block.entity.ForestryGreenhouseBlockEntity;
import com.list.block.entity.ForestryHybridizerBlockEntity;
import com.list.block.entity.SellingBinBlockEntity;
import com.list.block.entity.TreeCompostBlockEntity;
import com.list.block.entity.TapperBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.list.ListMod.REGISTRATE;

public class ModBlockEntities {
    public static final BlockEntityEntry<FishPondCoreBlockEntity> FISH_POND_CORE = REGISTRATE
        .blockEntity("fishpond_core", FishPondCoreBlockEntity::new)
        .validBlocks(ModBlocks.FISHPOND_CORE)
        .register();

    public static final BlockEntityEntry<TreeCompostBlockEntity> TREE_COMPOST = REGISTRATE
        .blockEntity("tree_compost", (BlockEntityType<TreeCompostBlockEntity> type, BlockPos pos, BlockState state) -> new TreeCompostBlockEntity(type, pos, state))
        .validBlocks(ModBlocks.TREE_COMPOST)
        .register();

    public static final BlockEntityEntry<ForestryGreenhouseBlockEntity> FORESTRY_GREENHOUSE = REGISTRATE
        .blockEntity("forestry_greenhouse", (BlockEntityType<ForestryGreenhouseBlockEntity> type, BlockPos pos, BlockState state) -> new ForestryGreenhouseBlockEntity(type, pos, state))
        .validBlocks(ModBlocks.FORESTRY_GREENHOUSE)
        .register();

    public static final BlockEntityEntry<ForestryHybridizerBlockEntity> FORESTRY_HYBRIDIZER = REGISTRATE
        .blockEntity("forestry_hybridizer", (BlockEntityType<ForestryHybridizerBlockEntity> type, BlockPos pos, BlockState state) -> new ForestryHybridizerBlockEntity(type, pos, state))
        .validBlocks(ModBlocks.FORESTRY_HYBRIDIZER)
        .register();

    public static final BlockEntityEntry<SellingBinBlockEntity> SELLING_BIN = REGISTRATE
        .blockEntity("selling_bin", SellingBinBlockEntity::new)
        .validBlocks(ModBlocks.SELLING_BIN)
        .register();

    public static final BlockEntityEntry<TapperBlockEntity> TAPPER = REGISTRATE
        .blockEntity("tapper", TapperBlockEntity::new)
        .validBlocks(ModBlocks.TAPPER)
        .register();

    public static void register() {
    }
}
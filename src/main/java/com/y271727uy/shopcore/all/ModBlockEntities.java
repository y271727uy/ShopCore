package com.y271727uy.shopcore.all;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.block.entity.SellingBinBlockEntity;
import com.y271727uy.shopcore.gameplay.tree.block.entity.TreeCompostBlockEntity;
import com.y271727uy.shopcore.gameplay.tree.block.entity.TreeStumpBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ShopcoreMod.MODID);

    public static final RegistryObject<BlockEntityType<SellingBinBlockEntity>> SELLING_BIN = BLOCK_ENTITY_TYPES.register(
            "selling_bin",
            () -> BlockEntityType.Builder.of(SellingBinBlockEntity::new, ModBlock.SELLING_BIN.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BlockEntity>> TREE_COMPOST = BLOCK_ENTITY_TYPES.register(
            "tree_compost",
            () -> BlockEntityType.Builder.of(TreeCompostBlockEntity::create, ModBlock.TREE_COMPOST.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<BlockEntity>> TREE_STUMP = BLOCK_ENTITY_TYPES.register(
            "tree_stump",
            () -> BlockEntityType.Builder.of(TreeStumpBlockEntity::create, ModBlock.TREE_STUMP.get()).build(null)
    );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}


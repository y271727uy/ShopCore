package com.y271727uy.shopcore.all;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.block.SellingBinBlock;
import com.y271727uy.shopcore.gameplay.tree.block.TreeCompostBlock;
import com.y271727uy.shopcore.gameplay.tree.block.TreeStumpBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlock {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ShopcoreMod.MODID);

    public static final RegistryObject<Block> SELLING_BIN = BLOCKS.register("selling_bin",
            () -> new SellingBinBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion()));

    public static final RegistryObject<Block> TREE_COMPOST = BLOCKS.register("tree_compost",
            () -> new TreeCompostBlock(BlockBehaviour.Properties.copy(Blocks.DIRT).noOcclusion()));

    public static final RegistryObject<Block> TREE_STUMP = BLOCKS.register("tree_stump",
            () -> new TreeStumpBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion()));

    private ModBlock() {
    }
}

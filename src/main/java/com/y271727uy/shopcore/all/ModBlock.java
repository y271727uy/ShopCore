package com.y271727uy.shopcore.all;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.block.SellingBinBlock;
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

    public static final RegistryObject<Block> IRON_SELLING_BIN = BLOCKS.register("iron_selling_bin",
            () -> new SellingBinBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion(), 8 * 60 * 20, "iron_sellingbin"));

    public static final RegistryObject<Block> GOLD_SELLING_BIN = BLOCKS.register("gold_selling_bin",
            () -> new SellingBinBlock(BlockBehaviour.Properties.copy(Blocks.GOLD_BLOCK).noOcclusion(), 6 * 60 * 20, "gold_sellingbin"));

    public static final RegistryObject<Block> DIAMOND_SELLING_BIN = BLOCKS.register("diamond_selling_bin",
            () -> new SellingBinBlock(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK).noOcclusion(), 4 * 60 * 20, "diamond_sellingbin"));

    public static final RegistryObject<Block> NETHERITE_SELLING_BIN = BLOCKS.register("netherite_selling_bin",
            () -> new SellingBinBlock(BlockBehaviour.Properties.copy(Blocks.NETHERITE_BLOCK).noOcclusion(), 2 * 60 * 20, "netherite_sellingbin"));

    private ModBlock() {
    }
}

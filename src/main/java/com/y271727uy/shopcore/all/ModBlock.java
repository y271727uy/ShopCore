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
            () -> new SellingBinBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    private ModBlock() {
    }
}

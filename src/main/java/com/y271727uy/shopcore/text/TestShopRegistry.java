package com.y271727uy.shopcore.text;

import com.y271727uy.shopcore.ShopcoreMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TestShopRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ShopcoreMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShopcoreMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ShopcoreMod.MODID);

    public static final RegistryObject<Block> TEST_SHOP = BLOCKS.register("test_shop",
            () -> new TestShopBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion()));

    public static final RegistryObject<Item> TEST_SHOP_ITEM = ITEMS.register("test_shop",
            () -> new BlockItem(TEST_SHOP.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<TestShopBlockEntity>> TEST_SHOP_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("test_shop",
                    () -> BlockEntityType.Builder.of(TestShopBlockEntity::new, TEST_SHOP.get()).build(null));

    private TestShopRegistry() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}

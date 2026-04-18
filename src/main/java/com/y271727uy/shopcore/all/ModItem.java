package com.y271727uy.shopcore.all;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.item.GlowingItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItem {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShopcoreMod.MODID);

	public static final RegistryObject<Item> BANK_CARD = ITEMS
			.register("bank_card", () -> new Item(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> PREMIUM_BANK_CARD = ITEMS
			.register("premium_bank_card", () -> new Item(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> SELLING_BIN = ITEMS
			.register("selling_bin", () -> new BlockItem(ModBlock.SELLING_BIN.get(), new Item.Properties()));

	public static final RegistryObject<Item> EQUALS = ITEMS
			.register("equals", () -> new GlowingItem(new Item.Properties()));


	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}

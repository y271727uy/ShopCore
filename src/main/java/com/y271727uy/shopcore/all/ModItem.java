package com.y271727uy.shopcore.all;

import com.y271727uy.shopcore.ShopcoreMod;
import com.y271727uy.shopcore.item.GlowingItem;
import com.y271727uy.shopcore.item.AgricultureDailyItem;
import com.y271727uy.shopcore.item.TestShopDailyItem;
import com.y271727uy.shopcore.item.card.BankCardItem;
import com.y271727uy.shopcore.item.card.PremiumBankCardItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItem {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShopcoreMod.MODID);

	public static final RegistryObject<Item> BANK_CARD = ITEMS
			.register("bank_card", () -> new BankCardItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> PREMIUM_BANK_CARD = ITEMS
			.register("premium_bank_card", () -> new PremiumBankCardItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> SELLING_BIN = ITEMS
			.register("selling_bin", () -> new com.y271727uy.shopcore.item.SellingBinBlockItem(ModBlock.SELLING_BIN.get(), new Item.Properties()));

	public static final RegistryObject<Item> IRON_SELLING_BIN = ITEMS
			.register("iron_selling_bin", () -> new com.y271727uy.shopcore.item.SellingBinBlockItem(ModBlock.IRON_SELLING_BIN.get(), new Item.Properties()));

	public static final RegistryObject<Item> GOLD_SELLING_BIN = ITEMS
			.register("gold_selling_bin", () -> new com.y271727uy.shopcore.item.SellingBinBlockItem(ModBlock.GOLD_SELLING_BIN.get(), new Item.Properties()));

	public static final RegistryObject<Item> DIAMOND_SELLING_BIN = ITEMS
			.register("diamond_selling_bin", () -> new com.y271727uy.shopcore.item.SellingBinBlockItem(ModBlock.DIAMOND_SELLING_BIN.get(), new Item.Properties()));

	public static final RegistryObject<Item> NETHERITE_SELLING_BIN = ITEMS
			.register("netherite_selling_bin", () -> new com.y271727uy.shopcore.item.SellingBinBlockItem(ModBlock.NETHERITE_SELLING_BIN.get(), new Item.Properties()));

	public static final RegistryObject<Item> EQUALS = ITEMS
			.register("equals", () -> new GlowingItem(new Item.Properties()));

	public static final RegistryObject<Item> AGRICULTURE_DAILY = ITEMS
			.register("agriculture_daily", () -> new AgricultureDailyItem(new Item.Properties().stacksTo(1)));

	public static final RegistryObject<Item> TEST_SHOP_DAILY = ITEMS
			.register("test_shop_daily", () -> new TestShopDailyItem(new Item.Properties().stacksTo(1)));


	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
}

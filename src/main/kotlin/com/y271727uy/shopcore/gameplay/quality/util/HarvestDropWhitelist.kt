@file:Suppress("unused")

package com.y271727uy.shopcore.gameplay.quality.util

/**
 * 旧的通用非标准收获表已拆分。
 *
 * - 浆果 / 果树：`BerryFruitHarvestWhitelist`
 * - 非标准 crop：`NonStandardCropWhitelist`
 */
@Deprecated("Use BerryFruitHarvestWhitelist", ReplaceWith("BerryFruitHarvestWhitelist"))
object HarvestDropWhitelist {
	@JvmStatic
	fun matches(stack: net.minecraft.world.item.ItemStack, block: net.minecraft.world.level.block.Block): Boolean =
		BerryFruitHarvestWhitelist.matches(stack, block)

	@JvmStatic
	fun matches(stack: net.minecraft.world.item.ItemStack): Boolean = BerryFruitHarvestWhitelist.matches(stack)

	@JvmStatic
	fun matches(block: net.minecraft.world.level.block.Block): Boolean = BerryFruitHarvestWhitelist.matches(block)

	@JvmStatic
	fun install(config: BerryFruitHarvestWhitelist.Config) {
		BerryFruitHarvestWhitelist.install(config)
	}

	@JvmStatic
	fun clear() {
		BerryFruitHarvestWhitelist.clear()
	}

	@JvmStatic
	fun current(): BerryFruitHarvestWhitelist.Config = BerryFruitHarvestWhitelist.current()
}

@Deprecated("Use berryFruitHarvestWhitelist", ReplaceWith("berryFruitHarvestWhitelist(block)"))
fun harvestDropWhitelist(block: BerryFruitHarvestWhitelist.Builder.() -> Unit): BerryFruitHarvestWhitelist.Config = berryFruitHarvestWhitelist(block)





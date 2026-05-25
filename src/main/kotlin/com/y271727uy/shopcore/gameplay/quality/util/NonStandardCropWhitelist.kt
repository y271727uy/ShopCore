@file:Suppress("unused")

package com.y271727uy.shopcore.gameplay.quality.util

import net.minecraft.world.level.block.Block

/**
 * 非标准 crop 破坏白名单：一条配置同时绑定“掉落物 id + 方块 id”。
 *
 * 只给“破坏即收获”的非标准 crop 使用。
 */
object NonStandardCropWhitelist {
	internal data class Entry(
		val dropItemId: net.minecraft.resources.ResourceLocation,
		val blockId: net.minecraft.resources.ResourceLocation,
	)

	@Volatile
	private var active: Config = createDefaultConfig()

	fun nonStandardCropWhitelist(block: Builder.() -> Unit): Config = Builder().apply(block).build()

	fun install(config: Config) {
		active = config
	}

	fun clear() {
		active = Config.EMPTY
	}

	fun current(): Config = active

	fun matches(stack: net.minecraft.world.item.ItemStack, block: Block): Boolean = active.matches(stack, block)

	fun matches(stack: net.minecraft.world.item.ItemStack): Boolean = active.matches(stack)

	fun matches(block: Block): Boolean = active.matches(block)

	class Builder {
		private val entries = linkedSetOf<Entry>()

		fun WhiteListItem(dropItemId: String, blockId: String) {
			val drop = parseResourceLocation(dropItemId)
			val blockIdRl = parseResourceLocation(blockId)
			if (drop != null && blockIdRl != null) {
				entries.add(Entry(drop, blockIdRl))
			}
		}

		fun build(): Config = Config(entries.toSet())

		private fun parseResourceLocation(raw: String): net.minecraft.resources.ResourceLocation? {
			return net.minecraft.resources.ResourceLocation.tryParse(raw.trim().removePrefix("#"))
		}
	}

	class Config internal constructor(
		private val entries: Set<Entry>,
	) {
		val isEmpty: Boolean
			get() = entries.isEmpty()

		fun matches(stack: net.minecraft.world.item.ItemStack, block: Block): Boolean {
			if (stack.isEmpty) {
				return false
			}

			val blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block) ?: return false
			val itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.item) ?: return false
			return entries.any { entry -> entry.blockId == blockId && entry.dropItemId == itemId }
		}

		fun matches(stack: net.minecraft.world.item.ItemStack): Boolean {
			if (stack.isEmpty) {
				return false
			}

			val itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.item) ?: return false
			return entries.any { entry -> entry.dropItemId == itemId }
		}

		fun matches(block: Block): Boolean {
			val blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block) ?: return false
			return entries.any { entry -> entry.blockId == blockId }
		}

		companion object {
			val EMPTY: Config = Config(emptySet())
		}
	}

	private fun createDefaultConfig(): Config = Config.EMPTY
}

fun nonStandardCropWhitelist(block: NonStandardCropWhitelist.Builder.() -> Unit): NonStandardCropWhitelist.Config = NonStandardCropWhitelist.nonStandardCropWhitelist(block)





@file:Suppress("unused")

package com.y271727uy.shopcore.gameplay.quality.util

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries

/**
 * Manors Bounty 专用浆果 / 果树白名单。
 *
 * 这个对象只负责 manors_bounty 的收获目标，避免继续污染通用白名单。
 */
object MDWhiteList {
	internal data class Entry(
		val dropItemId: ResourceLocation,
		val blockId: ResourceLocation,
	)

	@Volatile
	private var active: Config = createDefaultConfig()

	fun mdWhiteList(block: Builder.() -> Unit): Config = Builder().apply(block).build()

	fun install(config: Config) {
		active = config
	}

	fun clear() {
		active = createDefaultConfig()
	}

	fun current(): Config = active

	fun matches(stack: ItemStack, block: Block): Boolean = active.matches(stack, block)

	fun matches(stack: ItemStack): Boolean = active.matches(stack)

	fun matches(block: Block): Boolean = active.matches(block)

	fun expectedDropId(block: Block): ResourceLocation? = active.expectedDropId(block)

	class Builder {
		private val entries = linkedSetOf<Entry>()

		fun WhiteListItem(dropItemId: String, blockId: String) {
			val drop = parseResourceLocation(dropItemId)
			val block = parseResourceLocation(blockId)
			if (drop != null && block != null) {
				entries.add(Entry(drop, block))
			}
		}

		fun build(): Config = Config(entries.toSet())

		private fun parseResourceLocation(raw: String): ResourceLocation? {
			return ResourceLocation.tryParse(raw.trim().removePrefix("#"))
		}
	}

	class Config internal constructor(
		private val entries: Set<Entry>,
	) {
		val isEmpty: Boolean
			get() = entries.isEmpty()

		fun matches(stack: ItemStack, block: Block): Boolean {
			if (stack.isEmpty) {
				return false
			}

			val blockId = ForgeRegistries.BLOCKS.getKey(block) ?: return false
			val itemId = ForgeRegistries.ITEMS.getKey(stack.item) ?: return false
			return entries.any { entry -> entry.blockId == blockId && entry.dropItemId == itemId }
		}

		fun matches(stack: ItemStack): Boolean {
			if (stack.isEmpty) {
				return false
			}

			val itemId = ForgeRegistries.ITEMS.getKey(stack.item) ?: return false
			return entries.any { entry -> entry.dropItemId == itemId }
		}

		fun matches(block: Block): Boolean {
			val blockId = ForgeRegistries.BLOCKS.getKey(block) ?: return false
			return entries.any { entry -> entry.blockId == blockId }
		}

		fun expectedDropId(block: Block): ResourceLocation? {
			val blockId = ForgeRegistries.BLOCKS.getKey(block) ?: return null
			return entries.firstOrNull { entry -> entry.blockId == blockId }?.dropItemId
		}

		companion object {
			val EMPTY: Config = Config(emptySet())
		}
	}

	private fun createDefaultConfig(): Config {
		return mdWhiteList {
			WhiteListItem("manors_bounty:olive_fruit", "manors_bounty:olive_tree_leaves")
		}
	}
}

fun mdWhiteList(block: MDWhiteList.Builder.() -> Unit): MDWhiteList.Config = MDWhiteList.mdWhiteList(block)

@file:Suppress("unused")

package com.y271727uy.shopcore.gameplay.quality.util

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.ForgeRegistries

/**
 * 质量白名单配置与匹配入口。
 *
 * 典型用法：
 * ```kotlin
 * val config = standardCropWhitelist {
 *     WhiteListTag("forge:crops")
 *     WhiteListItem("minecraft:wheat")
 * }
 * StandardCropWhitelist.install(config)
 * ```
 */
object StandardCropWhitelist {
	@Volatile
	private var active: Config = createDefaultStandardCropConfig()

	fun standardCropWhitelist(block: Builder.() -> Unit): Config = Builder().apply(block).build()

	fun install(config: Config) {
		active = config
	}

	fun clear() {
		active = Config.EMPTY
	}

	fun current(): Config = active

	fun matches(stack: ItemStack): Boolean = active.matches(stack)

	class Builder {
		private val itemIds = linkedSetOf<ResourceLocation>()
		private val tagIds = linkedSetOf<ResourceLocation>()

		fun WhiteListItem(id: String) {
			parseResourceLocation(id)?.let(itemIds::add)
		}

		fun WhiteListTag(id: String) {
			parseResourceLocation(id)?.let(tagIds::add)
		}

		fun build(): Config = Config(itemIds.toSet(), tagIds.toSet())

		private fun parseResourceLocation(raw: String): ResourceLocation? {
			val normalized = raw.trim().removePrefix("#")
			return ResourceLocation.tryParse(normalized)
		}
	}

	class Config internal constructor(
		private val itemIds: Set<ResourceLocation>,
		tagIds: Set<ResourceLocation>,
	) {
		private val tagKeys: Set<TagKey<Item>> = tagIds.mapTo(linkedSetOf()) { tagId -> TagKey.create(Registries.ITEM, tagId) }

		val isEmpty: Boolean
			get() = itemIds.isEmpty() && tagKeys.isEmpty()

		fun matches(stack: ItemStack): Boolean {
			if (stack.isEmpty) {
				return false
			}

			val item = stack.item
			val itemId = ForgeRegistries.ITEMS.getKey(item) ?: return false
			if (itemId in itemIds) {
				return true
			}

			if (tagKeys.isEmpty()) {
				return false
			}

			return tagKeys.any { tagKey -> stack.`is`(tagKey) }
		}

		fun matches(item: Item): Boolean {
			val itemId = ForgeRegistries.ITEMS.getKey(item) ?: return false
			return itemId in itemIds
		}

		companion object {
			val EMPTY: Config = Config(emptySet(), emptySet())
		}
	}

	private fun createDefaultStandardCropConfig(): Config {
		return standardCropWhitelist {
			WhiteListItem("minecraft:wheat")
			WhiteListItem("minecraft:carrot")
			WhiteListItem("minecraft:potato")
			WhiteListItem("minecraft:beetroot")
		}
	}
}

fun standardCropWhitelist(block: StandardCropWhitelist.Builder.() -> Unit): StandardCropWhitelist.Config = StandardCropWhitelist.standardCropWhitelist(block)




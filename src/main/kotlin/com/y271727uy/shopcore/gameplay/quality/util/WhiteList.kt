@file:Suppress("unused")

package com.y271727uy.shopcore.gameplay.quality.util

/**
 * 兼容旧名字的外壳，新的标准作物白名单请使用 `StandardCropWhitelist`。
 */
@Deprecated("Use StandardCropWhitelist", ReplaceWith("StandardCropWhitelist"))
object WhiteList {
	@JvmStatic
	fun configure(block: Builder.() -> Unit): Config = StandardCropWhitelist.standardCropWhitelist(block)

	@JvmStatic
	fun install(config: Config) {
		StandardCropWhitelist.install(config)
	}

	@JvmStatic
	fun clear() {
		StandardCropWhitelist.clear()
	}

	@JvmStatic
	fun current(): Config = StandardCropWhitelist.current()

	@JvmStatic
	fun matches(stack: net.minecraft.world.item.ItemStack): Boolean = StandardCropWhitelist.matches(stack)
}

typealias Builder = StandardCropWhitelist.Builder
typealias Config = StandardCropWhitelist.Config

@Deprecated("Use standardCropWhitelist", ReplaceWith("standardCropWhitelist(block)"))
fun configure(block: Builder.() -> Unit): Config = StandardCropWhitelist.standardCropWhitelist(block)


package com.y271727uy.shopcore.gameplay.tree.event.EnvironmentVariables

import com.y271727uy.shopcore.integration.sereneseasons.SereneSeasonsCompat
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

@Suppress("unused")
object SeasonVariables {
	private val TREE_GROWTH_SEASONS = setOf("spring", "summer")
	private val SPRING_CROPS = TagKey.create(Registries.BLOCK, ResourceLocation("sereneseasons", "spring_crops"))
	private val SUMMER_CROPS = TagKey.create(Registries.BLOCK, ResourceLocation("sereneseasons", "summer_crops"))
	private val AUTUMN_CROPS = TagKey.create(Registries.BLOCK, ResourceLocation("sereneseasons", "autumn_crops"))
	private val WINTER_CROPS = TagKey.create(Registries.BLOCK, ResourceLocation("sereneseasons", "winter_crops"))
	private val SEASON_TAGS: Map<String, TagKey<Block>> = mapOf(
		"spring" to SPRING_CROPS,
		"summer" to SUMMER_CROPS,
		"autumn" to AUTUMN_CROPS,
		"winter" to WINTER_CROPS
	)

	@JvmStatic
	fun currentSeasonId(level: Level?, pos: BlockPos?): String? {
		return SereneSeasonsCompat.getCurrentSeasonId(level, pos).orElse(null)
	}

	@JvmStatic
	fun isTreeGrowthAllowed(level: Level?, pos: BlockPos?): Boolean {
		if (level == null) {
			return false
		}
		if (pos == null) {
			val seasonId = currentSeasonId(level, null) ?: return false
			return seasonId in TREE_GROWTH_SEASONS
		}

		val state = level.getBlockState(pos)
		val matchedSeasons = SEASON_TAGS.filterValues { tag -> state.`is`(tag) }.keys
		if (matchedSeasons.size == SEASON_TAGS.size) {
			return true
		}
		if (matchedSeasons.isEmpty()) {
			val seasonId = currentSeasonId(level, pos) ?: currentSeasonId(level, null) ?: return false
			return seasonId in TREE_GROWTH_SEASONS
		}

		val seasonId = currentSeasonId(level, pos) ?: currentSeasonId(level, null) ?: return false
		return seasonId in matchedSeasons
	}
}

// 和 serene-seasons-YXTfAkOf.jar 的季节系统兼容
// 这里作为树逻辑的轻量包装，后续可以直接复用这些方法做季节判断

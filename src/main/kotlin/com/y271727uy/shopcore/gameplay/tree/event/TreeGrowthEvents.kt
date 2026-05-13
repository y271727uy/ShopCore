package com.y271727uy.shopcore.gameplay.tree.event

import com.y271727uy.shopcore.ShopcoreMod
import com.y271727uy.shopcore.gameplay.tree.block.entity.TreeCompostBlockEntity
import net.minecraft.server.level.ServerLevel
import net.minecraftforge.event.entity.player.BonemealEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@Suppress("unused")
object TreeGrowthEvents {
	@JvmStatic
	@SubscribeEvent
	fun onBonemeal(event: BonemealEvent) {
		val level = event.level as? ServerLevel ?: return
		val saplingPos = event.pos
		val compost = level.getBlockEntity(saplingPos.below()) as? TreeCompostBlockEntity ?: return
		if (!isManagedSapling(level, saplingPos)) {
			return
		}

		event.isCanceled = true
		if (!com.y271727uy.shopcore.gameplay.tree.event.EnvironmentVariables.SeasonVariables.isTreeGrowthAllowed(level, saplingPos)) {
			return
		}
		TreeCompostBlockEntity.forceMature(level, saplingPos.below(), compost)
	}

	@JvmStatic
	fun isManagedSapling(level: net.minecraft.world.level.Level, saplingPos: net.minecraft.core.BlockPos): Boolean {
		val compost = level.getBlockEntity(saplingPos.below()) as? TreeCompostBlockEntity ?: return false
		return compost.matchesSapling(level)
	}
}



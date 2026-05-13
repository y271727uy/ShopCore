package com.y271727uy.shopcore.gameplay.tree.event

import com.y271727uy.shopcore.ShopcoreMod
import net.minecraft.world.level.Level
import net.minecraftforge.event.level.SaplingGrowTreeEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@Suppress("unused")
object TreeVanillaGrowthEvents {

	@JvmStatic
	@SubscribeEvent
	fun onSaplingGrowTree(event: SaplingGrowTreeEvent) {
		val level = event.level as? Level ?: return
		if (level.isClientSide) {
			return
		}

		if (!TreeGrowthEvents.isManagedSapling(level, event.pos)) {
			return
		}

		event.isCanceled = true
	}
}

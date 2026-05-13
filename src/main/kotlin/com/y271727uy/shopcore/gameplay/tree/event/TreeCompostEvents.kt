package com.y271727uy.shopcore.gameplay.tree.event

import com.y271727uy.shopcore.ShopcoreMod
import com.y271727uy.shopcore.all.ModBlock
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@Suppress("unused")
object TreeCompostEvents {

	@JvmStatic
	@SubscribeEvent
	fun onBlockBreak(event: BlockEvent.BreakEvent) {
		val level = event.level
		if (level.isClientSide) {
			return
		}

		if (level.getBlockState(event.pos).block != ModBlock.TREE_COMPOST.get()) {
			return
		}

		val abovePos = event.pos.above()
		if (!level.getBlockState(abovePos).isAir) {
			level.destroyBlock(abovePos, false)
		}
	}
}

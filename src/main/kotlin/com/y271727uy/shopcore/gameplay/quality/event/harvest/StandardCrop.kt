package com.y271727uy.shopcore.gameplay.quality.event.harvest

import com.y271727uy.shopcore.ShopcoreMod
import com.y271727uy.shopcore.gameplay.quality.Quality
import com.y271727uy.shopcore.gameplay.quality.QualityNbt
import com.y271727uy.shopcore.gameplay.quality.util.StandardCropWhitelist
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.CropBlock
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = ShopcoreMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
@Suppress("unused")
object StandardCrop {

	@JvmStatic
	@SubscribeEvent
	fun onBlockBreak(event: BlockEvent.BreakEvent) {
		val level = event.level
		if (level.isClientSide || level !is ServerLevel) {
			return
		}

		val state = level.getBlockState(event.pos)
		val block = state.block
		if (block !is CropBlock || !block.isMaxAge(state)) {
			return
		}

		event.isCanceled = true

		val drops = Block.getDrops(state, level, event.pos, level.getBlockEntity(event.pos))
		level.removeBlock(event.pos, false)

		for (stack in drops) {
			if (stack.isEmpty) {
				continue
			}

			if (QualityNbt.hasValidQuality(stack) || !StandardCropWhitelist.matches(stack)) {
				Block.popResource(level, event.pos, stack)
				continue
			}

			rollQuality(level)?.let { quality ->
				QualityNbt.write(stack, quality)
			}

			Block.popResource(level, event.pos, stack)
		}
	}

	private fun rollQuality(level: ServerLevel): Quality? {
		return when (level.random.nextInt(100)) {
			in 0 until 25 -> Quality.IRON
			in 25 until 40 -> Quality.GOLD
			in 40 until 45 -> Quality.DIAMOND
			else -> null
		}
	}
}
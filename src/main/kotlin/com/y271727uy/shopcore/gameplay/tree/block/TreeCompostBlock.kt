package com.y271727uy.shopcore.gameplay.tree.block

import com.y271727uy.shopcore.gameplay.tree.block.entity.TreeCompostBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as BlockProperties
import net.minecraft.world.level.block.state.BlockState
import com.y271727uy.shopcore.all.ModBlockEntities

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class TreeCompostBlock(properties: BlockProperties) : BaseEntityBlock(properties) {
	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
		return TreeCompostBlockEntity.create(pos, state)
	}

	override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
		return if (level.isClientSide) null else createTickerHelper(type, ModBlockEntities.TREE_COMPOST.get()) { currentLevel, pos, currentState, blockEntity ->
			TreeCompostBlockEntity.serverTick(currentLevel, pos, currentState, blockEntity as TreeCompostBlockEntity)
		}
	}

	@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

	override fun getRenderShape(state: BlockState): RenderShape {
		return RenderShape.MODEL
	}
}
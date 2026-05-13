package com.y271727uy.shopcore.gameplay.tree.block

import com.y271727uy.shopcore.gameplay.tree.block.entity.TreeStumpBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as BlockProperties
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import com.y271727uy.shopcore.all.ModBlockEntities

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class TreeStumpBlock(properties: BlockProperties) : BaseEntityBlock(properties) {
	override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? {
		return TreeStumpBlockEntity.create(pos, state)
	}

	override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
		return if (level.isClientSide) null else createTickerHelper(type, ModBlockEntities.TREE_STUMP.get()) { currentLevel, pos, currentState, blockEntity ->
			TreeStumpBlockEntity.serverTick(currentLevel, pos, currentState, blockEntity as TreeStumpBlockEntity)
		}
	}

	@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
	override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
		if (state.block != newState.block) {
			val blockEntity = level.getBlockEntity(pos)
			if (blockEntity is TreeStumpBlockEntity) {
				blockEntity.clearTree()
			}
		}
		super.onRemove(state, level, pos, newState, isMoving)
	}

	@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
	override fun getRenderShape(state: BlockState): RenderShape {
		return RenderShape.MODEL
	}
}
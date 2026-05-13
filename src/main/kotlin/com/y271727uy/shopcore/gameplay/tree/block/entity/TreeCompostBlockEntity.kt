package com.y271727uy.shopcore.gameplay.tree.block.entity

import com.y271727uy.shopcore.all.ModBlock
import com.y271727uy.shopcore.all.ModBlockEntities
import com.y271727uy.shopcore.gameplay.tree.TreeDefinitions
import com.y271727uy.shopcore.gameplay.tree.event.EnvironmentVariables.SeasonVariables
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.registries.ForgeRegistries

class TreeCompostBlockEntity private constructor(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.TREE_COMPOST.get(), pos, state) {
	var treeType: String = ""
	var treeShape: Int = 0
	var dy: Int = 0
	var pendingGrowthTicks: Int = -1

	companion object {
		private const val INITIAL_GROWTH_DELAY_MIN_TICKS = 20 * 10
		private const val INITIAL_GROWTH_DELAY_MAX_TICKS = 20 * 30

		@JvmStatic
		fun create(pos: BlockPos, state: BlockState): BlockEntity {
			return TreeCompostBlockEntity(pos, state)
		}

		@JvmStatic
		fun serverTick(level: net.minecraft.world.level.Level, pos: BlockPos, state: BlockState, entity: TreeCompostBlockEntity) {
			if (level.isClientSide || level !is ServerLevel) {
				return
			}

			if (state.block != ModBlock.TREE_COMPOST.get()) {
				return
			}

			if (!entity.validateAndNormalize()) {
				return
			}

			val abovePos = pos.above()
			val aboveState = level.getBlockState(abovePos)
			val aboveId = ForgeRegistries.BLOCKS.getKey(aboveState.block)?.toString().orEmpty()

			if (entity.treeType.isNotEmpty() && aboveId != entity.treeType) {
				entity.resetTreeData()
				entity.syncToClient()
				return
			}

			if (entity.pendingGrowthTicks >= 0 && !SeasonVariables.isTreeGrowthAllowed(level, abovePos)) {
				return
			}

			if (entity.pendingGrowthTicks < 0) {
				return
			}

			if (entity.pendingGrowthTicks > 0) {
				entity.pendingGrowthTicks -= 1
				return
			}

			level.removeBlock(abovePos, false)
			if (TreeDefinitions.createTree(level, abovePos, entity.treeType, entity.dy)) {
				entity.pendingGrowthTicks = -1
				entity.syncToClient()
			}
		}

		@JvmStatic
		fun scheduleInitialGrowth(entity: TreeCompostBlockEntity, random: RandomSource) {
			val range = INITIAL_GROWTH_DELAY_MAX_TICKS - INITIAL_GROWTH_DELAY_MIN_TICKS + 1
			entity.pendingGrowthTicks = random.nextInt(range) + INITIAL_GROWTH_DELAY_MIN_TICKS
			entity.syncToClient()
		}

		@JvmStatic
		fun forceMature(level: ServerLevel, pos: BlockPos, entity: TreeCompostBlockEntity): Boolean {
			if (!entity.validateAndNormalize()) {
				return false
			}
			if (!SeasonVariables.isTreeGrowthAllowed(level, pos.above())) {
				return false
			}
			level.removeBlock(pos.above(), false)
			val grew = TreeDefinitions.createTree(level, pos.above(), entity.treeType, entity.dy)
			if (grew) {
				entity.pendingGrowthTicks = -1
				entity.syncToClient()
			}
			return grew
		}
	}

	fun validateAndNormalize(): Boolean {
		val definition = TreeDefinitions.treeDefinitions[treeType] ?: run {
			resetTreeData()
			return false
		}

		treeShape = definition.shapeIndex
		dy = dy.coerceIn(-1, 1)
		return true
	}

	fun matchesSapling(level: net.minecraft.world.level.Level): Boolean {
		if (treeType.isEmpty()) {
			return false
		}
		val aboveId = ForgeRegistries.BLOCKS.getKey(level.getBlockState(worldPosition.above()).block)?.toString().orEmpty()
		return aboveId == treeType
	}

	private fun resetTreeData() {
		treeType = ""
		treeShape = 0
		dy = 0
		pendingGrowthTicks = -1
	}

	private fun syncToClient() {
		setChanged()
		(level as? ServerLevel)?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
	}

	private fun readTreeData(tag: CompoundTag) {
		treeType = tag.getString("tree_type")
		treeShape = tag.getInt("tree_shape")
		dy = tag.getInt("dy")
		pendingGrowthTicks = if (tag.contains("pending_growth_ticks")) tag.getInt("pending_growth_ticks") else -1
		validateAndNormalize()
	}

	private fun writeTreeData(tag: CompoundTag) {
		tag.putString("tree_type", treeType)
		tag.putInt("tree_shape", treeShape)
		tag.putInt("dy", dy)
		tag.putInt("pending_growth_ticks", pendingGrowthTicks)
	}

	override fun saveAdditional(tag: CompoundTag) {
		super.saveAdditional(tag)
		writeTreeData(tag)
	}

	override fun load(tag: CompoundTag) {
		super.load(tag)
		readTreeData(tag)
	}

	override fun getUpdateTag(): CompoundTag {
		val tag = super.getUpdateTag()
		writeTreeData(tag)
		return tag
	}

	override fun getUpdatePacket(): ClientboundBlockEntityDataPacket? {
		return ClientboundBlockEntityDataPacket.create(this)
	}

	override fun handleUpdateTag(tag: CompoundTag) {
		super.handleUpdateTag(tag)
		readTreeData(tag)
	}

	override fun onDataPacket(net: Connection, pkt: ClientboundBlockEntityDataPacket) {
		val tag = pkt.tag
		if (tag != null) {
			handleUpdateTag(tag)
		}
	}
}

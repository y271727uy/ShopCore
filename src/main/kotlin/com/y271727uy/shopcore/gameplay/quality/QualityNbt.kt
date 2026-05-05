package com.y271727uy.shopcore.gameplay.quality

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

/**
 * quality NBT 读写与校验入口。
 */
object QualityNbt {
    const val QUALITY_KEY: String = "quality"

    @JvmStatic
    fun parse(value: Int): Quality? = Quality.fromId(value)

    @JvmStatic
    fun parse(tag: CompoundTag?): Quality? {
        if (tag == null || !tag.contains(QUALITY_KEY)) {
            return null
        }

        return parse(tag.getInt(QUALITY_KEY))
    }

    @JvmStatic
    fun get(stack: ItemStack): Quality? = parse(stack.tag)

    @JvmStatic
    fun getQualityId(stack: ItemStack): Int? = get(stack)?.id

    @JvmStatic
    fun hasValidQuality(stack: ItemStack): Boolean = get(stack) != null

    @JvmStatic
    fun write(tag: CompoundTag, quality: Quality): CompoundTag {
        tag.putInt(QUALITY_KEY, quality.id)
        return tag
    }

    @JvmStatic
    fun write(stack: ItemStack, quality: Quality): ItemStack {
        write(stack.orCreateTag, quality)
        return stack
    }

    @JvmStatic
    fun clear(stack: ItemStack) {
        val tag = stack.tag ?: return
        tag.remove(QUALITY_KEY)
    }
}



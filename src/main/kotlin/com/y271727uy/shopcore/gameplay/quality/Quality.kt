package com.y271727uy.shopcore.gameplay.quality

import com.y271727uy.shopcore.ShopcoreMod
import net.minecraft.resources.ResourceLocation

/**
 * 品质档位：仅允许 1 / 2 / 3，对应 NBT 字段 quality1 / quality2 / quality3。
 * 这是一个注解（算了别在意），因为实现了Quality作物，所以我认为我可以写和Quality Food不兼容（？）
 */
@Suppress("unused")
enum class Quality(
    val id: Int,
    val textureName: String,
    val nbtKey: String,
    val priceBonusMin: Int,
    val priceBonusMax: Int,
) {
    IRON(1, "iron_star", "quality1", 1, 1),
    GOLD(2, "gold_star", "quality2", 2, 2),
    DIAMOND(3, "diamond_star", "quality3", 3, 4);

    val textureLocation: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
        ShopcoreMod.MODID,
        "textures/item/$textureName.png"
    )

    val priceBonusRange: IntRange = priceBonusMin..priceBonusMax

    companion object {
        @JvmStatic
        fun fromId(id: Int): Quality? = values().firstOrNull { it.id == id }

        @JvmStatic
        fun fromNbtKey(nbtKey: String): Quality? = values().firstOrNull { it.nbtKey == nbtKey }
    }
}


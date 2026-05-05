package com.y271727uy.shopcore.gameplay.quality

import com.y271727uy.shopcore.ShopcoreMod
import net.minecraft.resources.ResourceLocation

/**
 * 品质档位：仅允许 1 / 2 / 3。
 */
enum class Quality(val id: Int, val textureName: String) {
    IRON(1, "iron_star"),
    GOLD(2, "gold_star"),
    DIAMOND(3, "diamond_star");

    val textureLocation: ResourceLocation = ResourceLocation.fromNamespaceAndPath(
        ShopcoreMod.MODID,
        "textures/item/$textureName.png"
    )

    companion object {
        @JvmStatic
        fun fromId(id: Int): Quality? = values().firstOrNull { it.id == id }
    }
}


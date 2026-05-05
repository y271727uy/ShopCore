package com.y271727uy.shopcore.gameplay.quality.client

import com.y271727uy.shopcore.gameplay.quality.QualityNbt
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack

/**
 * 根据 ItemStack 的 quality NBT，实时叠加对应星标贴图。
 *
 * 这里不做任何缓存：每次渲染都直接读当前 stack 的 NBT。
 */
object QualityOverlayRenderer {
    @JvmStatic
    fun render(guiGraphics: GuiGraphics, stack: ItemStack, x: Int, y: Int) {
        val quality = QualityNbt.get(stack) ?: return

        val poseStack = guiGraphics.pose()
        poseStack.pushPose()
        poseStack.translate(-1.0, 0.0, 200.0)
        poseStack.translate((x + 8).toDouble(), (y + 8).toDouble(), 0.0)
        poseStack.scale(1.0f, 1.0f, 1.0f)
        guiGraphics.blit(quality.textureLocation, -8, -8, 0.0f, 0.0f, 16, 16, 16, 16)
        poseStack.popPose()
    }
}





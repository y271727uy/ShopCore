package com.y271727uy.shopcore.client.order;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class OrderPromptHudRenderer {
    private static final int X = 40;
    private static final int BASE_Y = 17 * 12;
    private static final int STEP_Y = 12;
    private static final float SCALE = 2.0F;

    private OrderPromptHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        List<ItemStack> stacks = OrderPromptClientState.displayStacks();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            int y = BASE_Y - STEP_Y * i;
            graphics.pose().pushPose();
            graphics.pose().translate(X, y, 0);
            graphics.pose().scale(SCALE, SCALE, 1.0F);
            graphics.renderItem(stack, 0, 0);
            graphics.renderItemDecorations(minecraft.font, stack, 0, 0);
            graphics.pose().popPose();
        }
    }
}

package com.list.client.screen;

import com.list.ListMod;
import com.list.menu.ForestryHybridizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ForestryHybridizerScreen extends AbstractContainerScreen<ForestryHybridizerMenu> {

    public static final ResourceLocation BACKGROUND = ListMod.rl("textures/gui/screen/forestry_hybridizer_gui.png");

    public ForestryHybridizerScreen(ForestryHybridizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);

        int uOffset = 177 + menu.getProgress(4) * 4;
        guiGraphics.blit(BACKGROUND, i + 21, j + 20, uOffset, 0, 2, menu.getProgress(48), 256, 256);

        guiGraphics.blit(BACKGROUND, i + 99, j + 37, 176, 46, menu.getProgress(22), 15, 256, 256);
    }
}


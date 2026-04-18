package com.list.client.screen;

import com.list.ListMod;
import com.list.menu.FishPondMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FishPondScreen extends AbstractContainerScreen<FishPondMenu> {

    public static final ResourceLocation BACKGROUND = ListMod.rl("textures/gui/screen/fishpond.png");
    public static final ResourceLocation PROGRESS = ListMod.rl("textures/gui/screen/fishpond_progress.png");

    public FishPondScreen(FishPondMenu menu, Inventory playerInventory, Component title) {
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
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        guiGraphics.blit(PROGRESS,i + 73, j + 22,0, 0, menu.getProgress(42), 46, 42, 46);
    }
}

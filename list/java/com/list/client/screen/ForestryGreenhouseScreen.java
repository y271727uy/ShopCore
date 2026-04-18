package com.list.client.screen;

import com.list.ListMod;
import com.list.menu.ForestryGreenhouseMenu;
import com.list.util.FluidHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

/**
 * Forestry Greenhouse GUI.
 * Background texture is 256x256, but we only blit the standard 176x166 area.
 */
public class ForestryGreenhouseScreen extends AbstractContainerScreen<ForestryGreenhouseMenu> {

    public static final ResourceLocation BACKGROUND = ListMod.rl("textures/gui/screen/greenhouse_gui.png");

    public ForestryGreenhouseScreen(ForestryGreenhouseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 175;
        this.imageHeight = 204;
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
        // The texture is 256x256, and our GUI uses the top-left 176x166 like vanilla.
        guiGraphics.blit(BACKGROUND, i, j, 0, 0, this.imageWidth, this.imageHeight);

        int uOffset = 177 + menu.getProgress(4) * 4;
        guiGraphics.blit(BACKGROUND, i + 8, j + 32, uOffset, 0, 2, menu.getProgress(48), 256, 256);

        guiGraphics.blit(BACKGROUND, i + 89, j + 47, 176, 60, menu.getProgress(24), 16);

        // fluid
        float fluidPersent = menu.getWaterMb() / 16000f;
        float drawnU = 0;
        float drawnV = 1.0f - fluidPersent;
        float drawnWidth = 1.0f;
        float drawnHeight = fluidPersent;
        drawFluid(
            guiGraphics,
            new FluidStack(Fluids.WATER, menu.getWaterMb()),
            i + 70 + drawnU * 16,
            j + 23 + drawnV * 58,
            16 * drawnWidth,
            58 * drawnHeight,
            -1
        );

        // burnTime
        int burnTime = menu.getBurnTime(14);
        if (burnTime > 0) {
            // 91, 69
            guiGraphics.blit(BACKGROUND, i + 90, j + 69 + 14 - burnTime, 176, 46 + 14 - burnTime, 14, burnTime, 256, 256);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // vanilla positions: title at y=6, inventory title at y=(imageHeight - 94)
        // Move inventory title down by +2.
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94 + 2, 0x404040, false);
    }

    public static void drawFluidTexture(VertexConsumer buffer, PoseStack.Pose pose, float xCoord, float yCoord, TextureAtlasSprite textureSprite, float maskTop, float maskRight, float zLevel, int fluidColor) {
        float uMin = textureSprite.getU0();
        float uMax = textureSprite.getU1();
        float vMin = textureSprite.getV0();
        float vMax = textureSprite.getV1();
        uMax = uMax - maskRight / 16f * (uMax - uMin);
        vMax = vMax - maskTop / 16f * (vMax - vMin);

        var mat = pose.pose();
        buffer.vertex(mat, xCoord, yCoord + 16, zLevel).uv(uMin, vMax).color(fluidColor).endVertex();
        buffer.vertex(mat, xCoord + 16 - maskRight, yCoord + 16, zLevel).uv(uMax, vMax).color(fluidColor).endVertex();
        buffer.vertex(mat, xCoord + 16 - maskRight, yCoord + maskTop, zLevel).uv(uMax, vMin).color(fluidColor).endVertex();
        buffer.vertex(mat, xCoord, yCoord + maskTop, zLevel).uv(uMin, vMin).color(fluidColor).endVertex();
    }

    private static void drawFluid(GuiGraphics graphics, FluidStack contents, float startX, float startY, float widthT, float heightT, int color) {
        ResourceLocation LOCATION_BLOCKS_TEXTURE = InventoryMenu.BLOCK_ATLAS;
        TextureAtlasSprite fluidStillSprite = FluidHelper.getStillTexture(contents);
        if (fluidStillSprite == null) {
            fluidStillSprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
        }

        int fluidColor = FluidHelper.getColor(contents) | 0xff000000;
        if (color != -1) {
            fluidColor = FastColor.ARGB32.multiply(fluidColor, color);
        }

        RenderSystem.setShaderTexture(0, LOCATION_BLOCKS_TEXTURE);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableDepthTest();

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        final int xTileCount = (int) (widthT / 16);
        final float xRemainder = widthT - xTileCount * 16;
        final int yTileCount = (int) (heightT / 16);
        final float yRemainder = heightT - yTileCount * 16;

        final float yStart = startY + heightT;

        for (int xTile = 0; xTile <= xTileCount; xTile++) {
            for (int yTile = 0; yTile <= yTileCount; yTile++) {
                float width = xTile == xTileCount ? xRemainder : 16;
                float height = yTile == yTileCount ? yRemainder : 16;
                float x = startX + xTile * 16;
                float y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    float maskTop = 16 - height;
                    float maskRight = 16 - width;
                    drawFluidTexture(builder, graphics.pose().last(), x, y, fluidStillSprite, maskTop, maskRight, 0, fluidColor);
                }
            }
        }
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableBlend();
    }
}


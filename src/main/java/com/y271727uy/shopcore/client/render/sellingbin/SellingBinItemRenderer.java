package com.y271727uy.shopcore.client.render.sellingbin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.y271727uy.shopcore.client.render.model.SellingBinModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class SellingBinItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final SellingBinModel model;

    public SellingBinItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        this.model = new SellingBinModel(Minecraft.getInstance().getEntityModels().bakeLayer(SellingBinModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext displayContext, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        switch (displayContext) {
            case GUI, FIXED -> {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.92F, 0.92F, 0.92F);
            }
            case GROUND -> {
                poseStack.translate(0.5D, 0.35D, 0.5D);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.5D, 0.55D, 0.5D);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.78F, 0.78F, 0.78F);
            }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poseStack.translate(0.5D, 0.6D, 0.5D);
                poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                poseStack.scale(0.82F, 0.82F, 0.82F);
            }
            default -> {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
        }

        poseStack.scale(1.0F, -1.0F, -1.0F);

        model.applyAnimation(0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(SellingBinModel.TEXTURE_LOCATION));
        model.renderToBuffer(poseStack, consumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}


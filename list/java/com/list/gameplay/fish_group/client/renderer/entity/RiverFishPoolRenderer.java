package com.list.gameplay.fish_group.client.renderer.entity;

import com.list.ListMod;
import com.list.gameplay.fish_group.client.model.entity.RiverFishPoolModel;
import com.list.gameplay.fish_group.entity.RiverFishPoolEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

public class RiverFishPoolRenderer extends EntityRenderer<RiverFishPoolEntity> {
    private static final ResourceLocation TEXTURE = ListMod.rl("textures/entity/river_fish_pool.png");
    private final RiverFishPoolModel<RiverFishPoolEntity> model;

    public RiverFishPoolRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RiverFishPoolModel<>(context.bakeLayer(RiverFishPoolModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(RiverFishPoolEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RiverFishPoolEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotateX((float)Math.PI));
        Quaternionf rotationRandom = new Quaternionf().rotateY((float)Math.toRadians(entity.getRandomRotation()));
        poseStack.mulPose(rotationRandom);
        poseStack.translate(0, -3.5, 0);
        float ageInTicks = entity.tickCount + partialTicks;
        model.setupAnim(entity, 0.0f, 0.0f, ageInTicks, entityYaw, 0.0f);
        int overlay = entity.getHurtTime() > 0 ? OverlayTexture.RED_OVERLAY_V : OverlayTexture.NO_OVERLAY;
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(TEXTURE)), packedLight, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}


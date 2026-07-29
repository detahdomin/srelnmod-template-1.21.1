package com.shand1an.sreln.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.shand1an.sreln.entity.TargetDesignatorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class TargetDesignatorRenderer extends EntityRenderer<TargetDesignatorEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/block/beacon.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public TargetDesignatorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TargetDesignatorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float bob = (float) Math.sin((entity.tickCount + partialTicks) * 0.1F) * 0.05F;
        poseStack.translate(0.0D, 0.5D + bob, 0.0D);
        float rotation = (entity.tickCount + partialTicks) * 2.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        renderCube(matrix, consumer, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderCube(Matrix4f matrix, VertexConsumer consumer, int packedLight) {
        float min = -0.5F;
        float max = 0.5F;
        // Front face
        consumer.addVertex(matrix, min, min, max).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, max, min, max).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, max, max, max).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, 1);
        consumer.addVertex(matrix, min, max, max).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, 1);
        // Back face
        consumer.addVertex(matrix, max, min, min).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, min, min, min).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, min, max, min).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        consumer.addVertex(matrix, max, max, min).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 0, -1);
        // Top face
        consumer.addVertex(matrix, min, max, max).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, max, max, max).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, max, max, min).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, min, max, min).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        // Bottom face
        consumer.addVertex(matrix, min, min, min).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, max, min, min).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, max, min, max).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, -1, 0);
        consumer.addVertex(matrix, min, min, max).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, -1, 0);
        // Left face
        consumer.addVertex(matrix, min, min, min).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, min, min, max).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, min, max, max).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-1, 0, 0);
        consumer.addVertex(matrix, min, max, min).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(-1, 0, 0);
        // Right face
        consumer.addVertex(matrix, max, min, max).setColor(255, 255, 255, 200).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, max, min, min).setColor(255, 255, 255, 200).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, max, max, min).setColor(255, 255, 255, 200).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(1, 0, 0);
        consumer.addVertex(matrix, max, max, max).setColor(255, 255, 255, 200).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(1, 0, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(TargetDesignatorEntity entity) {
        return TEXTURE;
    }
}
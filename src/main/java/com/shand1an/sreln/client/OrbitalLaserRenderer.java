package com.shand1an.sreln.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shand1an.sreln.entity.OrbitalLaserEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class OrbitalLaserRenderer extends EntityRenderer<OrbitalLaserEntity> {

    private static final ResourceLocation BEAM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/beacon_beam.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(BEAM_TEXTURE);
    private static final float MIN_BEAM_RADIUS = 0.04F;
    private static final float MAX_BEAM_RADIUS = 1.5F;
    private static final float CORE_RADIUS = 0.3F;
    private static final int GROWTH_DURATION = 60;
    private static final int FLASH_DURATION = 10;
    private static final int RETRACT_DURATION = 20;
    private static final int MAX_Y = 320;
    private static final int BEAM_QUADS = 16;

    public OrbitalLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(OrbitalLaserEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        double x = entity.getX();
        double targetY = entity.getY();
        double z = entity.getZ();

        poseStack.pushPose();
        poseStack.translate(-x, 0, -z);

        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();

        int age = entity.tickCount;
        int lifetime = entity.getLifetime();
        boolean retracting = lifetime <= RETRACT_DURATION;
        float retractProgress = retracting ? (RETRACT_DURATION - lifetime) / (float) RETRACT_DURATION : 0.0F;

        float growth = Math.min(1.0F, (float) age / GROWTH_DURATION);
        float easedGrowth = growth < 0.5F
                ? 2.0F * growth * growth
                : 1.0F - (float) Math.pow(-2.0F * growth + 2.0F, 2) / 2.0F;

        float flashProgress = Math.min(1.0F, (float) age / FLASH_DURATION);
        float flashAlpha = (1.0F - flashProgress) * 0.8F * (1.0F - retractProgress);
        float flashRadius = 1.0F + (1.0F - flashProgress) * 2.0F;

        float radius = (MIN_BEAM_RADIUS + (MAX_BEAM_RADIUS - MIN_BEAM_RADIUS) * easedGrowth) * (1.0F - retractProgress);
        float beamTopY = MAX_Y;
        float beamBottomY = (float) (MAX_Y - (MAX_Y - targetY) * easedGrowth);
        if (retracting) {
            beamTopY = (float) (MAX_Y - (MAX_Y - beamBottomY) * retractProgress);
        }

        float alpha = (0.55F + 0.35F * easedGrowth) * (1.0F - retractProgress);
        float time = (entity.tickCount + partialTicks) * 0.15F;

        int fullBright = LightTexture.FULL_BRIGHT;
        float beamHeight = beamTopY - beamBottomY;
        float texV = beamHeight / 64.0F;

        float flashR = flashRadius;
        for (int i = 0; i < BEAM_QUADS; i++) {
            float angle = (float) (i * Math.PI / BEAM_QUADS) + time;
            float cos = Mth.cos(angle) * flashR;
            float sin = Mth.sin(angle) * flashR;
            float fx1 = (float) x + cos;
            float fz1 = (float) z + sin;
            float fx2 = (float) x - cos;
            float fz2 = (float) z - sin;
            float ftopU = (time * 0.5F) % 1.0F;
            renderBeamQuad(matrix, consumer, fx1, beamBottomY, fz1, fx2, beamTopY, fz2,
                    ftopU, 0.0F, ftopU, texV, flashAlpha, fullBright, 255, 255, 255);
        }

        for (int i = 0; i < BEAM_QUADS; i++) {
            float angle = (float) (i * Math.PI / BEAM_QUADS) + time;
            float cos = Mth.cos(angle) * radius;
            float sin = Mth.sin(angle) * radius;
            float x1 = (float) x + cos;
            float z1 = (float) z + sin;
            float x2 = (float) x - cos;
            float z2 = (float) z - sin;
            float topU = (time * 0.5F) % 1.0F;
            renderBeamQuad(matrix, consumer, x1, beamBottomY, z1, x2, beamTopY, z2,
                    topU, 0.0F, topU, texV, alpha, fullBright, 180, 255, 255);
        }

        float coreAlpha = 0.8F + 0.2F * easedGrowth;
        for (int i = 0; i < BEAM_QUADS; i++) {
            float angle = (float) (i * Math.PI / BEAM_QUADS) + time * 1.3F;
            float cos = Mth.cos(angle) * CORE_RADIUS;
            float sin = Mth.sin(angle) * CORE_RADIUS;
            float cx1 = (float) x + cos;
            float cz1 = (float) z + sin;
            float cx2 = (float) x - cos;
            float cz2 = (float) z - sin;
            float topU = (time * 0.7F) % 1.0F;
            renderBeamQuad(matrix, consumer, cx1, beamBottomY, cz1, cx2, beamTopY, cz2,
                    topU, 0.0F, topU, texV, coreAlpha, fullBright, 255, 255, 255);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBeamQuad(Matrix4f matrix, VertexConsumer consumer,
                                float x1, float y1, float z1,
                                float x2, float y2, float z2,
                                float u1, float v1, float u2, float v2,
                                float alpha, int packedLight, int r, int g, int b) {
        int a = (int) (alpha * 255);
        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setUv(u1, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setUv(u2, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(u2, v2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(OrbitalLaserEntity entity) {
        return BEAM_TEXTURE;
    }
}
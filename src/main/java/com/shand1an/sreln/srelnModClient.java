package com.shand1an.sreln;

import com.shand1an.sreln.block.ModBlockEntities;
import com.shand1an.sreln.block.corpse.CorpseBlockRenderer;
import com.shand1an.sreln.client.FakePlayerEntityRenderer;
import com.shand1an.sreln.client.OrbitalLaserRenderer;
import com.shand1an.sreln.client.TargetDesignatorRenderer;
import com.shand1an.sreln.entity.ModEntityTypes;
import com.shand1an.sreln.effect.ModMobEffects;
import com.shand1an.sreln.screen.ModMenuTypes;
import com.shand1an.sreln.screen.LightingConsoleScreen;
import com.shand1an.sreln.screen.OrbitalStrikeCannonScreen;
import com.shand1an.sreln.screen.TerminalScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = srelnMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = srelnMod.MODID, value = Dist.CLIENT)
public class srelnModClient {
    public srelnModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        srelnMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        srelnMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        EntityRenderers.register(ModEntityTypes.RECALL_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.LINGERING_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.FAKE_PLAYER.get(), FakePlayerEntityRenderer::new);
        EntityRenderers.register(ModEntityTypes.HARMING_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.SWAP_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.SPLINTER_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.SKYWARD_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.RICOCHET_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.ECHO_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.SWIFT_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.ASSAULT_ENDER_PEARL.get(), ThrownItemRenderer::new);
        EntityRenderers.register(ModEntityTypes.SUMMONED_WARDEN.get(), net.minecraft.client.renderer.entity.WardenRenderer::new);
        EntityRenderers.register(ModEntityTypes.TARGET_DESIGNATOR.get(), TargetDesignatorRenderer::new);
        EntityRenderers.register(ModEntityTypes.ORBITAL_LASER.get(), OrbitalLaserRenderer::new);
    }

    @SubscribeEvent
    public static void registerBER(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CORPSE.get(), CorpseBlockRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ORBITAL_STRIKE_CANNON.get(), OrbitalStrikeCannonScreen::new);
        event.register(ModMenuTypes.TERMINAL.get(), TerminalScreen::new);
        event.register(ModMenuTypes.LIGHTING_CONSOLE.get(), LightingConsoleScreen::new);
    }

    private static final ResourceLocation NULL_SKIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "textures/entity/null_skin.png");

    private static final Map<UUID, Entity> MIMIC_ENTITIES = new java.util.HashMap<>();

    private static final Field WALK_POSITION_FIELD;
    private static final Field WALK_SPEED_OLD_FIELD;

    static {
        Field pos = null;
        Field speedOld = null;
        try {
            pos = WalkAnimationState.class.getDeclaredField("position");
            pos.setAccessible(true);
            speedOld = WalkAnimationState.class.getDeclaredField("speedOld");
            speedOld.setAccessible(true);
        } catch (Exception e) {
            srelnMod.LOGGER.error("Failed to access WalkAnimationState fields for mimic animation", e);
        }
        WALK_POSITION_FIELD = pos;
        WALK_SPEED_OLD_FIELD = speedOld;
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        EntityType<?> mimicType = MimicHandler.getMimicType(player.getUUID());

        if (mimicType != null) {
            event.setCanceled(true);
            renderMimicEntity(event, player, mimicType);
            return;
        } else if (player.hasEffect(ModMobEffects.NULL_EFFECT)) {
            PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();
            model.jacket.visible = false;
            model.leftPants.visible = false;
            model.rightPants.visible = false;
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
            model.hat.visible = false;
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        EntityType<?> mimicType = MimicHandler.getMimicType(player.getUUID());

        if (mimicType != null) {
            return;
        } else {
            Entity cached = MIMIC_ENTITIES.remove(player.getUUID());
            if (cached != null) {
                cached.discard();
            }
        }

        if (!player.hasEffect(ModMobEffects.NULL_EFFECT)) return;

        PlayerRenderer renderer = event.getRenderer();
        PlayerModel<AbstractClientPlayer> model = renderer.getModel();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = event.getPartialTick();

        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);

        poseStack.pushPose();

        if (player.getPose() == Pose.SLEEPING) {
            Direction bedDir = player.getBedOrientation();
            if (bedDir != null) {
                float bedRot = bedDir.toYRot();
                poseStack.translate(player.getX() - player.xo, 0, player.getZ() - player.zo);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180 - bedRot));
            }
        } else if (player.isAutoSpinAttack()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(player.tickCount + partialTick));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
        poseStack.translate(0.0F, -1.501F, 0.0F);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(NULL_SKIN_TEXTURE));
        model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }

    private static void renderMimicEntity(RenderPlayerEvent event, Player player, EntityType<?> mimicType) {
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        float partialTick = event.getPartialTick();

        Entity entity = MIMIC_ENTITIES.get(player.getUUID());
        if (entity != null && !entity.getType().equals(mimicType)) {
            entity.discard();
            MIMIC_ENTITIES.remove(player.getUUID());
            entity = null;
        }
        if (entity == null) {
            entity = mimicType.create(player.level());
            if (entity == null) return;
            MIMIC_ENTITIES.put(player.getUUID(), entity);
        }

        entity.setPos(player.getX(), player.getY(), player.getZ());
        entity.setYRot(player.getVisualRotationYInDegrees());
        entity.setXRot(player.getXRot());
        entity.yRotO = player.yRotO;
        entity.xRotO = player.xRotO;
        entity.tickCount = player.tickCount;

        if (entity instanceof LivingEntity living) {
            living.yBodyRot = player.yBodyRot;
            living.yBodyRotO = player.yBodyRotO;
            living.yHeadRot = player.yHeadRot;
            living.yHeadRotO = player.yHeadRotO;
            living.attackAnim = player.attackAnim;
            living.oAttackAnim = player.oAttackAnim;

            living.walkAnimation.setSpeed(player.walkAnimation.speed());
            try {
                if (WALK_POSITION_FIELD != null) {
                    WALK_POSITION_FIELD.setFloat(living.walkAnimation, player.walkAnimation.position());
                }
                if (WALK_SPEED_OLD_FIELD != null) {
                    WALK_SPEED_OLD_FIELD.setFloat(living.walkAnimation,
                            WALK_SPEED_OLD_FIELD.getFloat(player.walkAnimation));
                }
            } catch (Exception ignored) {
            }
        }

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super Entity> renderer = dispatcher.getRenderer(entity);

        poseStack.pushPose();
        renderer.render(entity, Mth.lerp(partialTick, player.yRotO, player.getYRot()),
                partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
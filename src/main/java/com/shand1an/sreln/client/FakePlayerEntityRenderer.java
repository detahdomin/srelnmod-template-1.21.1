package com.shand1an.sreln.client;

import com.mojang.authlib.GameProfile;
import com.shand1an.sreln.entity.FakePlayerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class FakePlayerEntityRenderer extends LivingEntityRenderer<FakePlayerEntity, PlayerModel<FakePlayerEntity>> {

    public FakePlayerEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
    }

    @Override
    protected boolean shouldShowName(FakePlayerEntity entity) {
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(FakePlayerEntity entity) {
        return entity.getOwnerUUID()
                .map(uuid -> Minecraft.getInstance().getSkinManager()
                        .getInsecureSkin(new GameProfile(uuid, entity.getOwnerName())).texture())
                .orElse(DefaultPlayerSkin.getDefaultTexture());
    }
}
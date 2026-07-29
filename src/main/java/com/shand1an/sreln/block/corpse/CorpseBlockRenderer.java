package com.shand1an.sreln.block.corpse;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CorpseBlockRenderer extends GeoBlockRenderer<CorpseBlockEntity> {
    public CorpseBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new CorpseBlockModel());
    }

    @Override
    public boolean shouldRenderOffScreen(CorpseBlockEntity blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(CorpseBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
package com.shand1an.sreln.block.corpse;

import com.shand1an.sreln.srelnMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CorpseItemModel extends GeoModel<CorpseBlockItem> {
    @Override
    public ResourceLocation getModelResource(CorpseBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "geo/corpse/" + item.variant + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CorpseBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "textures/entity/corpse/" + item.variant + "/texture.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CorpseBlockItem item) {
        return ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "animations/corpse/" + item.variant + ".animation.json");
    }
}
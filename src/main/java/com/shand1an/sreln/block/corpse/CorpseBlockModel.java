package com.shand1an.sreln.block.corpse;

import com.shand1an.sreln.srelnMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CorpseBlockModel extends GeoModel<CorpseBlockEntity> {
    @Override
    public ResourceLocation getModelResource(CorpseBlockEntity be) {
        return ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "geo/corpse/" + be.getVariant() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CorpseBlockEntity be) {
        return ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "textures/entity/corpse/" + be.getVariant() + "/texture.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CorpseBlockEntity be) {
        return ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "animations/corpse/" + be.getVariant() + ".animation.json");
    }
}
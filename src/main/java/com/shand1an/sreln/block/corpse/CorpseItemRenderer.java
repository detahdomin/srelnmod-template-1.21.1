package com.shand1an.sreln.block.corpse;

import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CorpseItemRenderer extends GeoItemRenderer<CorpseBlockItem> {
    public CorpseItemRenderer() {
        super(new CorpseItemModel());
    }
}
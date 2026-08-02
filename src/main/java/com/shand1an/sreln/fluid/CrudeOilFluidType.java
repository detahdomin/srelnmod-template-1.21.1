package com.shand1an.sreln.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

public class CrudeOilFluidType extends FluidType {

    public CrudeOilFluidType() {
        super(Properties.create()
                .density(1500)
                .viscosity(6000)
                .temperature(350)
                .canSwim(false)
                .canDrown(false)
                .canExtinguish(false)
                .canConvertToSource(false)
                .sound(SoundAction.get("bucket_fill"), SoundEvents.BUCKET_FILL_LAVA)
                .sound(SoundAction.get("bucket_empty"), SoundEvents.BUCKET_EMPTY_LAVA));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public ResourceLocation getOverlayTexture() {
                return ResourceLocation.withDefaultNamespace("block/water_overlay");
            }

            @Override
            public int getTintColor() {
                return 0xFF_1A1A1A;
            }
        });
    }
}
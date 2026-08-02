package com.shand1an.sreln.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class CrudeOilLiquidBlock extends LiquidBlock {

    public CrudeOilLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 0, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0, false, true));
            living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0, false, true));
        }
        super.entityInside(state, level, pos, entity);
    }
}
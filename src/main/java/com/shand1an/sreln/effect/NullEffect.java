package com.shand1an.sreln.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class NullEffect extends MobEffect {
    public NullEffect() {
        super(MobEffectCategory.NEUTRAL, 0x000000);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3; i++) {
                double dx = (entity.getRandom().nextDouble() - 0.5) * 0.8;
                double dy = entity.getRandom().nextDouble() * 1.8;
                double dz = (entity.getRandom().nextDouble() - 0.5) * 0.8;
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        entity.getX() + dx, entity.getY() + dy, entity.getZ() + dz,
                        1, 0, 0, 0, 0.02);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
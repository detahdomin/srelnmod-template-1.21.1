package com.shand1an.sreln.entity;

import com.shand1an.sreln.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class SkywardEnderPearlEntity extends ThrowableItemProjectile {
    private boolean triggered = false;

    public SkywardEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SkywardEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.SKYWARD_ENDER_PEARL.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Skyward_Ender_Pearl.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && !triggered && this.tickCount > 1) {
            if (this.getDeltaMovement().y < 0) {
                triggered = true;
                doTeleport();
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && !triggered) {
            triggered = true;
            doTeleport();
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && !triggered) {
            triggered = true;
            doTeleport();
        }
        this.discard();
    }

    private void doTeleport() {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner) {
            livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            livingOwner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, false));
        }
        this.discard();
    }
}
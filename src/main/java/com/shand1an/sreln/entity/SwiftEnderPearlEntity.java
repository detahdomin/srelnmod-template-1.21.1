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

public class SwiftEnderPearlEntity extends ThrowableItemProjectile {

    public SwiftEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SwiftEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.SWIFT_ENDER_PEARL.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Swift_Ender_Pearl.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            teleportAndEffect();
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            teleportAndEffect();
        }
        this.discard();
    }

    private void teleportAndEffect() {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner) {
            livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
            livingOwner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1));
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
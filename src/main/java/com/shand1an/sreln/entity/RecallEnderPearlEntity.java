package com.shand1an.sreln.entity;

import com.shand1an.sreln.item.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class RecallEnderPearlEntity extends ThrowableItemProjectile {
    private Vec3 originalPosition;
    private int timer = 0;
    private boolean hasHit = false;

    public RecallEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public RecallEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.RECALL_ENDER_PEARL.get(), owner, level);
        this.originalPosition = owner.position();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Recall_Ender_Pearl.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (hasHit) {
            this.setDeltaMovement(0, 0, 0);
            if (!this.level().isClientSide) {
                timer++;
                if (timer >= 60) {
                    teleportBack();
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && !hasHit) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            hasHit = true;
            this.setInvisible(true);
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner && originalPosition != null) {
                livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && !hasHit) {
            hasHit = true;
            this.setInvisible(true);
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner && originalPosition != null) {
                livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    private void teleportBack() {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner && originalPosition != null) {
            livingOwner.teleportTo(originalPosition.x, originalPosition.y, originalPosition.z);
            this.level().playSound(null, originalPosition.x, originalPosition.y, originalPosition.z,
                    SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.75F);
        }
    }
}
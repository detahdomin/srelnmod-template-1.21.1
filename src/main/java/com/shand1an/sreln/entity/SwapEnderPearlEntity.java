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

public class SwapEnderPearlEntity extends ThrowableItemProjectile {
    private Vec3 targetPos;
    private int timer = 0;
    private boolean hasHit = false;

    public SwapEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SwapEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.SWAP_ENDER_PEARL.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Swap_Ender_Pearl.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (hasHit) {
            this.setDeltaMovement(0, 0, 0);
            if (!this.level().isClientSide) {
                timer++;
                if (timer >= 2) {
                    Entity owner = this.getOwner();
                    if (owner instanceof LivingEntity livingOwner && targetPos != null) {
                        livingOwner.teleportTo(targetPos.x, targetPos.y, targetPos.z);
                        this.level().playSound(null, targetPos.x, targetPos.y, targetPos.z,
                                SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && !hasHit) {
            hasHit = true;
            this.setInvisible(true);
            Entity owner = this.getOwner();
            Entity target = result.getEntity();
            if (owner instanceof LivingEntity livingOwner) {
                this.targetPos = target.position();
                Vec3 ownerPos = livingOwner.position();

                owner.stopRiding();
                target.stopRiding();

                target.teleportTo(ownerPos.x, ownerPos.y, ownerPos.z);
                this.level().playSound(null, ownerPos.x, ownerPos.y, ownerPos.z,
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        this.discard();
    }
}
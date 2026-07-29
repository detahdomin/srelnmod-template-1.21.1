package com.shand1an.sreln.entity;

import com.shand1an.sreln.item.ModItems;
import net.minecraft.core.Direction;
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

public class RicochetEnderPearlEntity extends ThrowableItemProjectile {
    private int bounceCount = 0;
    private boolean bounced = false;
    private double bx, by, bz;
    private int cooldown = 0;

    public RicochetEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public RicochetEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.RICOCHET_ENDER_PEARL.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Ricochet_Ender_Pearl.get();
    }

    @Override
    public void tick() {
        bounced = false;
        if (cooldown > 0) cooldown--;
        super.tick();
        if (bounced) {
            this.setPos(bx, by, bz);
            this.xo = bx;
            this.yo = by;
            this.zo = bz;
            bounced = false;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (cooldown > 0) return;

        bounceCount++;
        if (bounceCount >= 2) {
            if (!this.level().isClientSide) {
                Entity owner = this.getOwner();
                if (owner instanceof LivingEntity livingOwner) {
                    livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            this.discard();
        } else {
            double damping = 0.5;
            Direction dir = result.getDirection();
            Vec3 hitPos = result.getLocation();
            Vec3 motion = this.getDeltaMovement();
            double newX = (dir.getStepX() != 0 ? -motion.x : motion.x) * damping;
            double newY = (dir.getStepY() != 0 ? -motion.y : motion.y) * damping;
            double newZ = (dir.getStepZ() != 0 ? -motion.z : motion.z) * damping;
            this.setDeltaMovement(newX, newY, newZ);
            bx = hitPos.x + dir.getStepX() * 0.5;
            by = hitPos.y + dir.getStepY() * 0.5;
            bz = hitPos.z + dir.getStepZ() * 0.5;
            bounced = true;
            cooldown = 3;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
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
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

public class SplinterEnderPearlEntity extends ThrowableItemProjectile {
    private boolean isReal = false;

    public SplinterEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SplinterEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.SPLINTER_ENDER_PEARL.get(), owner, level);
    }

    public void setReal(boolean real) {
        this.isReal = real;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Splinter_Ender_Pearl.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide && isReal) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                livingOwner.teleportTo(this.getX(), this.getY(), this.getZ());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide && isReal) {
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
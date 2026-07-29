package com.shand1an.sreln.entity;

import com.shand1an.sreln.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class LingeringEnderPearlEntity extends ThrowableItemProjectile {
    private Vec3 originalPosition;
    private float throwYRot;
    private float throwXRot;

    public LingeringEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public LingeringEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.LINGERING_ENDER_PEARL.get(), owner, level);
        this.originalPosition = owner.position();
        this.throwYRot = owner.getYRot();
        this.throwXRot = owner.getXRot();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Lingering_Ender_Pearl.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            Entity owner = this.getOwner();
            if (owner instanceof Player player && originalPosition != null) {
                player.teleportTo(this.getX(), this.getY(), this.getZ());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));

                ServerLevel serverLevel = (ServerLevel) this.level();
                FakePlayerEntity clone = new FakePlayerEntity(ModEntityTypes.FAKE_PLAYER.get(), serverLevel);
                clone.copyFrom(player);
                clone.setPos(originalPosition.x, originalPosition.y, originalPosition.z);
                clone.setYRot(throwYRot);
                clone.yHeadRot = throwYRot;
                clone.setXRot(throwXRot);
                clone.setNoGravity(true);
                clone.setInvulnerable(true);
                serverLevel.addFreshEntity(clone);
            }
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner instanceof Player player && originalPosition != null) {
                player.teleportTo(this.getX(), this.getY(), this.getZ());
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));

                ServerLevel serverLevel = (ServerLevel) this.level();
                FakePlayerEntity clone = new FakePlayerEntity(ModEntityTypes.FAKE_PLAYER.get(), serverLevel);
                clone.copyFrom(player);
                clone.setPos(originalPosition.x, originalPosition.y, originalPosition.z);
                clone.setYRot(throwYRot);
                clone.yHeadRot = throwYRot;
                clone.setXRot(throwXRot);
                clone.setNoGravity(true);
                clone.setInvulnerable(true);
                serverLevel.addFreshEntity(clone);
            }
        }
        this.discard();
    }
}
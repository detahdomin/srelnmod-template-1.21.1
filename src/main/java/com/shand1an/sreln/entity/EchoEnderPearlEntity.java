package com.shand1an.sreln.entity;

import com.shand1an.sreln.item.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class EchoEnderPearlEntity extends ThrowableItemProjectile {

    public EchoEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public EchoEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.ECHO_ENDER_PEARL.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Echo_Ender_Pearl.get();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            spawnWarden();
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            spawnWarden();
        }
        this.discard();
    }

    private void spawnWarden() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;

        if (owner != null) {
            Warden warden = EntityType.WARDEN.spawn(serverLevel, owner.blockPosition(), MobSpawnType.SPAWN_EGG);
            if (warden != null) {
                warden.setYRot(owner.getYRot());
                warden.setXRot(owner.getXRot());
                warden.setPersistenceRequired();
            }
            owner.teleportTo(this.getX(), this.getY(), this.getZ());
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SCULK_SHRIEKER_SHRIEK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
package com.shand1an.sreln.entity;

import com.shand1an.sreln.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class OrbitalLaserEntity extends Entity {

    private static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(OrbitalLaserEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    private int lifetime = 200;
    private int initialLifetime = 200;
    private BlockPos targetPos = BlockPos.ZERO;
    private SoundInstance chixuSound;

    public OrbitalLaserEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public OrbitalLaserEntity(Level level, BlockPos target, int lifetime) {
        super(ModEntityTypes.ORBITAL_LASER.get(), level);
        this.targetPos = target;
        this.lifetime = lifetime;
        this.initialLifetime = lifetime;
        this.setPos(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFETIME, 200);
    }

    @Override
    public void tick() {
        super.tick();
        this.lifetime--;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_LIFETIME, this.lifetime);
        }
        if (this.level().isClientSide) {
            tickClient();
        }
        if (!this.level().isClientSide && this.lifetime <= 0) {
            this.discard();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        if (this.lifetime <= 160 && this.lifetime > 110) {
            if (chixuSound == null) {
                chixuSound = new AbstractSoundInstance(
                        ModSounds.JIGUANG_CHIXU.get(), SoundSource.AMBIENT, RandomSource.create()) {
                    {
                        this.x = getX();
                        this.y = getY();
                        this.z = getZ();
                        this.volume = 1.5F;
                        this.pitch = 1.0F;
                        this.looping = false;
                        this.attenuation = SoundInstance.Attenuation.LINEAR;
                    }
                };
                Minecraft.getInstance().getSoundManager().play(chixuSound);
            }
        }
        if (this.lifetime <= 0 && chixuSound != null) {
            Minecraft.getInstance().getSoundManager().stop(chixuSound);
            chixuSound = null;
        }
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public int getLifetime() {
        return this.level().isClientSide ? this.entityData.get(DATA_LIFETIME) : this.lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_LIFETIME, lifetime);
        }
    }

    public int getInitialLifetime() {
        return initialLifetime;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.lifetime = tag.getInt("Lifetime");
        this.initialLifetime = tag.getInt("InitLifetime");
        this.targetPos = new BlockPos(tag.getInt("TX"), tag.getInt("TY"), tag.getInt("TZ"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Lifetime", this.lifetime);
        tag.putInt("InitLifetime", this.initialLifetime);
        tag.putInt("TX", this.targetPos.getX());
        tag.putInt("TY", this.targetPos.getY());
        tag.putInt("TZ", this.targetPos.getZ());
    }
}
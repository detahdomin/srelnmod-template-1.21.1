package com.shand1an.sreln.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class SummonedWarden extends Warden {
    private static final String SUMMONER_KEY = "Summoner";
    private java.util.UUID summonerId;

    public SummonedWarden(EntityType<SummonedWarden> type, Level level) {
        super(type, level);
    }

    public void setSummoner(LivingEntity summoner) {
        this.summonerId = summoner.getUUID();
    }

    @Nullable
    public LivingEntity getSummoner() {
        if (this.summonerId == null) return null;
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.summonerId);
            return entity instanceof LivingEntity ? (LivingEntity) entity : null;
        }
        return null;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (this.summonerId != null && target.getUUID().equals(this.summonerId)) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.summonerId != null) {
            compound.putUUID(SUMMONER_KEY, this.summonerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID(SUMMONER_KEY)) {
            this.summonerId = compound.getUUID(SUMMONER_KEY);
        }
    }
}
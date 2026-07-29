package com.shand1an.sreln.entity;

import com.shand1an.sreln.effect.ModMobEffects;
import com.shand1an.sreln.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class AssaultEnderPearlEntity extends ThrowableItemProjectile {

    private static final double ASSAULT_RANGE = 16.0;
    private static final double BEHIND_DISTANCE = 1.5;

    private int pearlLevel = 1;

    public AssaultEnderPearlEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public AssaultEnderPearlEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.ASSAULT_ENDER_PEARL.get(), owner, level);
    }

    public void setPearlLevel(int level) {
        this.pearlLevel = Math.max(1, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.Assault_Ender_Pearl.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                        this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                        0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
            if (owner != null) {
                LivingEntity target = findNearestTarget(owner);
                if (target != null) {
                    assaultTarget(owner, target);
                } else {
                    Vec3 safePos = findSafeTeleportPos(new Vec3(this.getX(), this.getY(), this.getZ()));
                    owner.teleportTo(safePos.x, safePos.y, safePos.z);
                    this.level().playSound(null, safePos.x, safePos.y, safePos.z,
                            SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
        }
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
            if (owner != null && result.getEntity() instanceof LivingEntity target) {
                assaultTarget(owner, target);
            }
        }
        this.discard();
    }

    private LivingEntity findNearestTarget(LivingEntity owner) {
        AABB searchArea = this.getBoundingBox().inflate(ASSAULT_RANGE);
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : this.level().getEntities(this, searchArea)) {
            if (entity instanceof LivingEntity le && entity != owner) {
                double dist = this.distanceToSqr(entity);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = le;
                }
            }
        }
        return nearest;
    }

    private void assaultTarget(LivingEntity owner, LivingEntity target) {
        ServerLevel serverLevel = (ServerLevel) this.level();

        Vec3 lookAngle = target.getLookAngle();
        Vec3 behindPos = target.position().subtract(lookAngle.scale(BEHIND_DISTANCE));
        Vec3 safePos = findSafeTeleportPos(behindPos);
        Vec3 fromPos = owner.position();

        spawnPhantomBurst(serverLevel, fromPos);
        spawnShadowTrail(serverLevel, fromPos, behindPos);
        this.level().playSound(null, fromPos.x, fromPos.y, fromPos.z,
                SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 0.5F);

        owner.teleportTo(serverLevel, safePos.x, safePos.y, safePos.z,
                Set.of(), target.getYRot(), target.getXRot());
        owner.setDeltaMovement(Vec3.ZERO);
        owner.fallDistance = 0.0F;

        this.level().playSound(null, safePos.x, safePos.y, safePos.z,
                SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 1.8F);

        spawnPhantomBurst(serverLevel, safePos);

        if (this.pearlLevel >= 2) {
            this.level().playSound(null, safePos.x, safePos.y, safePos.z,
                    SoundEvents.WITHER_AMBIENT, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 0.5F);
            for (int delay = 0; delay <= 30; delay += 10) {
                Vec3 pos = safePos;
                serverLevel.getServer().tell(new net.minecraft.server.TickTask(delay, () -> {
                    spawnPhantomBurst(serverLevel, pos, 80, 2.5);
                }));
            }
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
            owner.addEffect(new MobEffectInstance(ModMobEffects.NULL_EFFECT, 60, 0, false, false));
        }
    }

    private void spawnShadowTrail(ServerLevel level, Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double dist = diff.length();
        int steps = (int) (dist * 6);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = from.x + diff.x * t;
            double y = from.y + diff.y * t + 1.0;
            double z = from.z + diff.z * t;
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    x, y, z, 1, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private void spawnPhantomBurst(ServerLevel level, Vec3 pos) {
        spawnPhantomBurst(level, pos, 40, 1.5);
    }

    private void spawnPhantomBurst(ServerLevel level, Vec3 pos, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double r = this.random.nextDouble() * radius;
            double dx = Math.cos(angle) * r;
            double dz = Math.sin(angle) * r;
            double dy = this.random.nextDouble() * 2.0;
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.x + dx, pos.y + dy, pos.z + dz,
                    1, 0, 0.05, 0, 0.05);
        }
    }

    private Vec3 findSafeTeleportPos(Vec3 desired) {
        Level level = this.level();
        BlockPos blockPos = BlockPos.containing(desired);

        if (isSafePosition(level, blockPos)) {
            return desired;
        }

        for (int dy = 1; dy <= 10; dy++) {
            BlockPos checkPos = blockPos.above(dy);
            if (isSafePosition(level, checkPos)) {
                return new Vec3(desired.x, checkPos.getY(), desired.z);
            }
        }

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                BlockPos checkPos = blockPos.offset(dx, 0, dz);
                if (isSafePosition(level, checkPos)) {
                    return new Vec3(checkPos.getX() + 0.5, desired.y, checkPos.getZ() + 0.5);
                }
            }
        }

        int groundY = blockPos.getY();
        while (groundY > level.getMinBuildHeight() && level.getBlockState(new BlockPos(blockPos.getX(), groundY, blockPos.getZ())).isAir()) {
            groundY--;
        }
        return new Vec3(desired.x, groundY + 1.0, desired.z);
    }

    private boolean isSafePosition(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }
}
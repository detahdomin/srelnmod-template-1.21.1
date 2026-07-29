package com.shand1an.sreln.block;

import com.shand1an.sreln.entity.OrbitalLaserEntity;
import com.shand1an.sreln.screen.OrbitalStrikeCannonMenu;
import com.shand1an.sreln.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OrbitalStrikeCannonBlockEntity extends BlockEntity implements MenuProvider {

    private static final int TOTAL_DURATION = 220;
    private static final int DAMAGE_START = 180;
    private static final int DAMAGE_END = 60;
    private static final double AREA_RADIUS = 30.0;

    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_DESIGNATORS = new HashMap<>();


    public static void registerDesignator(ResourceKey<Level> dimension, BlockPos pos) {
        ACTIVE_DESIGNATORS.computeIfAbsent(dimension, k -> new HashSet<>()).add(pos);
    }

    public static void unregisterDesignator(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> set = ACTIVE_DESIGNATORS.get(dimension);
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                ACTIVE_DESIGNATORS.remove(dimension);
            }
        }
    }

    private int useCount = 3;
    private boolean isFiring = false;
    private int fireTimer = 0;
    private int damageWave = 0;
    private int explosionSoundTimer = 0;
    private boolean laserSoundPlayed = false;
    @Nullable
    private BlockPos targetPos = null;
    @Nullable
    private OrbitalLaserEntity laserEntity = null;

    public OrbitalStrikeCannonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORBITAL_STRIKE_CANNON.get(), pos, state);
    }

    public int getUseCount() {
        return useCount;
    }

    public boolean isFiring() {
        return isFiring;
    }

    public boolean isExhausted() {
        return useCount <= 0;
    }

    public boolean isRedstoneActive() {
        return isFiring && fireTimer > 60;
    }

    @Nullable
    public BlockPos getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(@Nullable BlockPos pos) {
        this.targetPos = pos;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void startFiring() {
        if (useCount <= 0 || targetPos == null || isFiring) return;
        this.isFiring = true;
        this.fireTimer = TOTAL_DURATION;
        this.damageWave = 0;
        this.explosionSoundTimer = 0;
        this.laserSoundPlayed = false;
        setChanged();
        if (level != null) {
            BlockState litState = getBlockState().setValue(OrbitalStrikeCannonBlock.LIT, true);
            level.setBlock(worldPosition, litState, Block.UPDATE_ALL);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, OrbitalStrikeCannonBlockEntity entity) {
        if (!entity.isFiring) return;

        if (entity.fireTimer > 0) {
            entity.fireTimer--;
        }

        if (entity.targetPos != null && level instanceof ServerLevel serverLevel) {
            if (entity.fireTimer > 40 && (entity.laserEntity == null || entity.laserEntity.isRemoved())) {
                entity.laserEntity = new OrbitalLaserEntity(serverLevel, entity.targetPos, entity.fireTimer + 1);
                serverLevel.addFreshEntity(entity.laserEntity);
                entity.laserSoundPlayed = false;
            }

            if (entity.laserEntity != null && !entity.laserEntity.isRemoved()) {
                if (!entity.laserSoundPlayed && entity.fireTimer <= 200 && entity.fireTimer > 190) {
                    entity.laserSoundPlayed = true;
                    serverLevel.playSound(null, entity.targetPos, ModSounds.JIGUANG_FASHE.get(),
                            SoundSource.BLOCKS, 2.0F, 1.0F);
                }
            }

            if (entity.fireTimer == 60 && entity.laserEntity != null && !entity.laserEntity.isRemoved()) {
                entity.laserEntity.setLifetime(20);
                level.setBlock(pos, state.setValue(OrbitalStrikeCannonBlock.LIT, false), Block.UPDATE_ALL);
            }

            if (entity.fireTimer <= DAMAGE_START && entity.fireTimer > DAMAGE_END) {
                entity.explosionSoundTimer++;
                if (entity.explosionSoundTimer % 20 == 1) {
                    serverLevel.playSound(null, entity.targetPos, SoundEvents.GENERIC_EXPLODE.value(),
                            SoundSource.BLOCKS, 4.0F, 1.0F);
                    spawnExplosionParticles(serverLevel, entity.targetPos);
                }

                int wave = (DAMAGE_START - entity.fireTimer) / 20;
                if (wave > entity.damageWave) {
                    entity.damageWave = wave;
                    applyDamage(serverLevel, entity.targetPos);
                }
            }
        }

        if (entity.fireTimer == 0) {
            if (entity.laserEntity != null) {
                entity.laserEntity = null;
            }
            entity.isFiring = false;
            entity.useCount--;
            entity.setChanged();
            BlockState currentState = level.getBlockState(pos);
            level.sendBlockUpdated(pos, currentState, currentState, Block.UPDATE_ALL);
        }

        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    private static void applyDamage(ServerLevel level, BlockPos target) {
        AABB area = new AABB(
                target.getX() - AREA_RADIUS, target.getY(), target.getZ() - AREA_RADIUS,
                target.getX() + AREA_RADIUS + 1, target.getY() + 6, target.getZ() + AREA_RADIUS + 1);
        float damagePerWave = 50.0F / 5.0F;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            entity.hurt(level.damageSources().magic(), damagePerWave);
        }
    }

    private static void spawnExplosionParticles(ServerLevel level, BlockPos target) {
        double x = target.getX() + 0.5;
        double y = target.getY();
        double z = target.getZ() + 0.5;
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0, 0, 0, 0.5);
        for (int i = 0; i < 80; i++) {
            double ox = (level.random.nextDouble() - 0.5) * AREA_RADIUS * 2;
            double oy = level.random.nextDouble() * 4;
            double oz = (level.random.nextDouble() - 0.5) * AREA_RADIUS * 2;
            level.sendParticles(ParticleTypes.EXPLOSION, x + ox, y + oy, z + oz, 1, 0, 0, 0, 0.1);
        }
        for (int i = 0; i < 20; i++) {
            double ox = (level.random.nextDouble() - 0.5) * AREA_RADIUS * 2;
            double oz = (level.random.nextDouble() - 0.5) * AREA_RADIUS * 2;
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + ox, y + 1, z + oz, 1, 0, 0.05, 0, 0.02);
        }
    }

    @Nullable
    public static BlockPos findNearestTargetDesignator(Level level, BlockPos from) {
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;
        Set<BlockPos> designators = ACTIVE_DESIGNATORS.get(level.dimension());
        if (designators != null) {
            for (BlockPos pos : designators) {
                double dist = pos.distSqr(from);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = pos;
                }
            }
        }
        return nearest;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sreln_mod.orbital_strike_cannon");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new OrbitalStrikeCannonMenu(containerId, playerInv, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.useCount = tag.getInt("UseCount");
        this.isFiring = tag.getBoolean("Firing");
        this.fireTimer = tag.getInt("FireTimer");
        if (tag.contains("TargetX")) {
            this.targetPos = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        } else {
            this.targetPos = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("UseCount", this.useCount);
        tag.putBoolean("Firing", this.isFiring);
        tag.putInt("FireTimer", this.fireTimer);
        if (this.targetPos != null) {
            tag.putInt("TargetX", this.targetPos.getX());
            tag.putInt("TargetY", this.targetPos.getY());
            tag.putInt("TargetZ", this.targetPos.getZ());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("UseCount", this.useCount);
        tag.putBoolean("Firing", this.isFiring);
        if (this.targetPos != null) {
            tag.putInt("TargetX", this.targetPos.getX());
            tag.putInt("TargetY", this.targetPos.getY());
            tag.putInt("TargetZ", this.targetPos.getZ());
        }
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

package com.shand1an.sreln.block.terminal;

import com.shand1an.sreln.block.ModBlockEntities;
import com.shand1an.sreln.block.LightingConsoleBlockEntity;
import com.shand1an.sreln.screen.TerminalMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TerminalBlockEntity extends BlockEntity implements MenuProvider {

    private boolean active = false;
    private int progressTicks = 0;
    private int progressMax = 0;
    private String progressLabel = "";
    @Nullable
    private BlockPos consolePos;

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TERMINAL.get(), pos, state);
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            if (!active) {
                forceLightsOff();
            }
        }
    }

    @Nullable
    public BlockPos getConsolePos() { return consolePos; }

    public void setConsolePos(@Nullable BlockPos pos) {
        this.consolePos = pos;
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide && level.isLoaded(worldPosition)) {
            forceLightsOff();
        }
    }

    public void setLights(boolean on) {
        if (level == null || level.isClientSide) return;
        LightingConsoleBlockEntity console = findConsole();
        if (console != null) {
            console.setLights(on);
        }
    }

    public void forceLightsOff() {
        if (level == null || level.isClientSide) return;
        LightingConsoleBlockEntity console = findConsole();
        if (console != null) {
            console.forceLightsOff();
        }
    }

    private LightingConsoleBlockEntity findConsole() {
        if (consolePos != null) {
            if (!level.isLoaded(consolePos)) {
                level.getChunk(consolePos);
            }
            BlockEntity be = level.getBlockEntity(consolePos);
            if (be instanceof LightingConsoleBlockEntity console
                    && console.getTerminalPositions().contains(worldPosition)) {
                return console;
            }
        }
        int r = 16;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    mpos.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(mpos);
                    if (be instanceof LightingConsoleBlockEntity console
                            && console.getTerminalPositions().contains(worldPosition)) {
                        this.consolePos = console.getBlockPos().immutable();
                        setChanged();
                        return console;
                    }
                }
            }
        }
        return null;
    }

    public boolean isProgressing() { return progressTicks < progressMax; }

    public void startProgress(String label, int maxTicks) {
        this.progressLabel = label;
        this.progressMax = maxTicks;
        this.progressTicks = 0;
        setChanged();
    }

    public float getProgressPercent() {
        return progressMax > 0 ? (float) progressTicks / progressMax : 0f;
    }

    public String getProgressLabel() { return progressLabel; }

    public static void serverTick(TerminalBlockEntity be) {
        if (be.progressTicks < be.progressMax) {
            be.progressTicks++;
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("active", active);
        tag.putInt("progressTicks", progressTicks);
        tag.putInt("progressMax", progressMax);
        tag.putString("progressLabel", progressLabel);
        if (consolePos != null) {
            tag.putInt("consoleX", consolePos.getX());
            tag.putInt("consoleY", consolePos.getY());
            tag.putInt("consoleZ", consolePos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        active = tag.getBoolean("active");
        progressTicks = tag.getInt("progressTicks");
        progressMax = tag.getInt("progressMax");
        progressLabel = tag.getString("progressLabel");
        if (tag.contains("consoleX")) {
            consolePos = new BlockPos(tag.getInt("consoleX"), tag.getInt("consoleY"), tag.getInt("consoleZ"));
        } else {
            consolePos = null;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sreln_mod.terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new TerminalMenu(containerId, playerInv, this, player.getTags().contains("hacker"));
    }
}
package com.shand1an.sreln.block;

import com.shand1an.sreln.block.terminal.TerminalBlockEntity;
import com.shand1an.sreln.screen.LightingConsoleMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightingConsoleBlockEntity extends BlockEntity implements MenuProvider {

    private final List<BlockPos> terminalPositions = new ArrayList<>();
    private final Set<BlockPos> lampPositions = new HashSet<>();

    public LightingConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHTING_CONSOLE.get(), pos, state);
    }

    public void addTerminal(BlockPos pos) {
        if (terminalPositions.contains(pos)) return;
        terminalPositions.add(pos);
        setChanged();
        sync();
    }

    public int getTerminalCount() { return terminalPositions.size(); }

    public List<BlockPos> getTerminalPositions() { return terminalPositions; }

    public void addLamp(BlockPos pos) {
        if (lampPositions.contains(pos)) return;
        lampPositions.add(pos.immutable());
        setChanged();
        sync();
    }

    public void removeLamp(BlockPos pos) {
        if (lampPositions.remove(pos)) {
            setChanged();
            sync();
        }
    }

    public int getActiveTerminalCount() {
        if (terminalPositions.isEmpty()) return 0;
        Level level = getLevel();
        if (level == null) return 0;
        int count = 0;
        for (BlockPos pos : terminalPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TerminalBlockEntity terminal && terminal.isActive()) count++;
        }
        return count;
    }

    public boolean areAllTerminalsOn() {
        if (terminalPositions.isEmpty()) return true;
        Level level = getLevel();
        if (level == null) return false;
        for (BlockPos pos : terminalPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof TerminalBlockEntity terminal) || !terminal.isActive()) return false;
        }
        return true;
    }

    public void setLights(boolean on) {
        if (level == null || level.isClientSide) return;
        if (!areAllTerminalsOn()) return;
        setLightsInternal(on);
    }

    public void forceLightsOff() {
        if (level == null || level.isClientSide) return;
        setLightsInternal(false);
    }

    private void setLightsInternal(boolean on) {
        Set<BlockPos> toRemove = new HashSet<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (BlockPos pos : lampPositions) {
            mutable.set(pos);
            if (!level.isLoaded(mutable)) {
                level.getChunk(mutable); // ponytail: load chunk so getBlockState/setBlock don't see air
            }
            BlockState state = level.getBlockState(mutable);
            if (state.getBlock() instanceof RedstoneLampBlock || state.is(Blocks.REDSTONE_LAMP)) {
                level.setBlock(mutable, state.setValue(RedstoneLampBlock.LIT, on), Block.UPDATE_ALL);
            } else {
                toRemove.add(pos);
            }
        }
        if (!toRemove.isEmpty()) {
            lampPositions.removeAll(toRemove);
            setChanged();
            sync();
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            setChanged();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide && level.isLoaded(worldPosition)) {
            forceLightsOff();
            for (BlockPos pos : terminalPositions) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TerminalBlockEntity terminal) {
                    terminal.setConsolePos(null);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag termList = new ListTag();
        for (BlockPos pos : terminalPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            termList.add(posTag);
        }
        tag.put("terminals", termList);

        ListTag lampList = new ListTag();
        for (BlockPos pos : lampPositions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            lampList.add(posTag);
        }
        tag.put("lamps", lampList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        terminalPositions.clear();
        ListTag termList = tag.getList("terminals", Tag.TAG_COMPOUND);
        for (int i = 0; i < termList.size(); i++) {
            CompoundTag posTag = termList.getCompound(i);
            terminalPositions.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
        }

        lampPositions.clear();
        ListTag lampList = tag.getList("lamps", Tag.TAG_COMPOUND);
        for (int i = 0; i < lampList.size(); i++) {
            CompoundTag posTag = lampList.getCompound(i);
            lampPositions.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
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
        return Component.translatable("block.sreln_mod.lighting_console");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new LightingConsoleMenu(containerId, playerInv, this, player.getTags().contains("hacker"));
    }
}
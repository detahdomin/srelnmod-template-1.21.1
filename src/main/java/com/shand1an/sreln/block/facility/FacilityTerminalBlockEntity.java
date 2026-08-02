package com.shand1an.sreln.block.facility;

import com.shand1an.sreln.block.ModBlockEntities;
import com.shand1an.sreln.screen.FacilityTerminalMenu;
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
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FacilityTerminalBlockEntity extends BlockEntity implements MenuProvider {

    private final List<FacilityEntry> facilities = new ArrayList<>();

    public FacilityTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FACILITY_TERMINAL.get(), pos, state);
    }

    public record FacilityEntry(String name, BlockPos leverPos) {}

    public void addFacility(String name, BlockPos leverPos) {
        for (FacilityEntry entry : facilities) {
            if (entry.leverPos.equals(leverPos)) return;
            if (entry.name.equals(name)) return;
        }
        facilities.add(new FacilityEntry(name, leverPos.immutable()));
        setChanged();
        sync();
    }

    public void removeFacility(String name) {
        facilities.removeIf(e -> e.name.equals(name));
        setChanged();
        sync();
    }

    public int getFacilityCount() {
        return facilities.size();
    }

    public List<FacilityEntry> getFacilities() {
        return facilities;
    }

    public FacilityEntry findFacility(String name) {
        for (FacilityEntry entry : facilities) {
            if (entry.name.equals(name)) return entry;
        }
        return null;
    }

    public boolean toggleFacility(String name, boolean on) {
        FacilityEntry entry = findFacility(name);
        if (entry == null) return false;
        Level level = getLevel();
        if (level == null) return false;
        BlockPos pos = entry.leverPos;
        if (!level.isLoaded(pos)) {
            level.getChunk(pos);
        }
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof LeverBlock || block == Blocks.LEVER) {
            boolean current = state.getValue(BlockStateProperties.POWERED);
            if (current != on) {
                level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, on), Block.UPDATE_ALL);
                level.updateNeighborsAt(pos, block);
            }
            return true;
        }
        return false;
    }

    public boolean isLeverOn(String name) {
        FacilityEntry entry = findFacility(name);
        if (entry == null) return false;
        Level level = getLevel();
        if (level == null) return false;
        BlockState state = level.getBlockState(entry.leverPos);
        return state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);
    }

    public void resetAll() {
        for (FacilityEntry entry : facilities) {
            toggleFacility(entry.name, false);
        }
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (FacilityEntry entry : facilities) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("Name", entry.name);
            entryTag.putInt("LX", entry.leverPos.getX());
            entryTag.putInt("LY", entry.leverPos.getY());
            entryTag.putInt("LZ", entry.leverPos.getZ());
            list.add(entryTag);
        }
        tag.put("Facilities", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        facilities.clear();
        ListTag list = tag.getList("Facilities", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            String name = entryTag.getString("Name");
            BlockPos pos = new BlockPos(entryTag.getInt("LX"), entryTag.getInt("LY"), entryTag.getInt("LZ"));
            facilities.add(new FacilityEntry(name, pos));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Facility Terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new FacilityTerminalMenu(containerId, playerInv, this, player.getTags().contains("hacker"));
    }
}
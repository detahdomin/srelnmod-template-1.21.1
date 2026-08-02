package com.shand1an.sreln.screen;

import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.facility.FacilityTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FacilityTerminalMenu extends AbstractContainerMenu {

    public final FacilityTerminalBlockEntity blockEntity;
    public final boolean isHacker;
    private final ContainerLevelAccess access;

    public FacilityTerminalMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData.readBlockPos()), extraData.readBoolean());
    }

    public FacilityTerminalMenu(int containerId, Inventory playerInv, FacilityTerminalBlockEntity blockEntity, boolean isHacker) {
        super(ModMenuTypes.FACILITY_TERMINAL.get(), containerId);
        this.blockEntity = blockEntity;
        this.isHacker = isHacker;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static FacilityTerminalBlockEntity getBlockEntity(Inventory playerInv, BlockPos pos) {
        Level level = playerInv.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FacilityTerminalBlockEntity terminal) {
            return terminal;
        }
        throw new IllegalStateException("Block entity is not FacilityTerminalBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.FACILITY_TERMINAL.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < 100) {
            var facilities = blockEntity.getFacilities();
            if (id < facilities.size()) {
                blockEntity.toggleFacility(facilities.get(id).name(), true);
                return true;
            }
        }
        if (id >= 100 && id < 200) {
            int idx = id - 100;
            var facilities = blockEntity.getFacilities();
            if (idx < facilities.size()) {
                blockEntity.toggleFacility(facilities.get(idx).name(), false);
                return true;
            }
        }
        if (id == 200) {
            if (player.getTags().contains("hacker")) {
                blockEntity.resetAll();
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }
}
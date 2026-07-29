package com.shand1an.sreln.screen;

import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.terminal.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TerminalMenu extends AbstractContainerMenu {

    public final TerminalBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public TerminalMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData.readBlockPos()));
    }

    public TerminalMenu(int containerId, Inventory playerInv, TerminalBlockEntity blockEntity) {
        super(ModMenuTypes.TERMINAL.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static TerminalBlockEntity getBlockEntity(Inventory playerInv, BlockPos pos) {
        Level level = playerInv.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TerminalBlockEntity terminal) {
            return terminal;
        }
        throw new IllegalStateException("Block entity is not TerminalBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.TERMINAL.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            this.blockEntity.setActive(true);
            return true;
        }
        if (id == 1) {
            this.blockEntity.setActive(false);
            return true;
        }
        return super.clickMenuButton(player, id);
    }
}
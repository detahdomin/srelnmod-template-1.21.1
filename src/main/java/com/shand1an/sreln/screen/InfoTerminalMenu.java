package com.shand1an.sreln.screen;

import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.info.InfoTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class InfoTerminalMenu extends AbstractContainerMenu {

    public final InfoTerminalBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public InfoTerminalMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData.readBlockPos()));
    }

    public InfoTerminalMenu(int containerId, Inventory playerInv, InfoTerminalBlockEntity blockEntity) {
        super(ModMenuTypes.INFO_TERMINAL.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static InfoTerminalBlockEntity getBlockEntity(Inventory playerInv, BlockPos pos) {
        Level level = playerInv.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof InfoTerminalBlockEntity it) {
            return it;
        }
        throw new IllegalStateException("Block entity is not InfoTerminalBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.INFO_TERMINAL.get());
    }
}
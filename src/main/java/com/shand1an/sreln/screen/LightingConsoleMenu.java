package com.shand1an.sreln.screen;

import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.LightingConsoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LightingConsoleMenu extends AbstractContainerMenu {

    public final LightingConsoleBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public LightingConsoleMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData.readBlockPos()));
    }

    public LightingConsoleMenu(int containerId, Inventory playerInv, LightingConsoleBlockEntity blockEntity) {
        super(ModMenuTypes.LIGHTING_CONSOLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    private static LightingConsoleBlockEntity getBlockEntity(Inventory playerInv, BlockPos pos) {
        Level level = playerInv.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof LightingConsoleBlockEntity console) {
            return console;
        }
        throw new IllegalStateException("Block entity is not LightingConsoleBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.LIGHTING_CONSOLE.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            this.blockEntity.setLights(true);
            return true;
        }
        if (id == 1) {
            this.blockEntity.setLights(false);
            return true;
        }
        return super.clickMenuButton(player, id);
    }
}
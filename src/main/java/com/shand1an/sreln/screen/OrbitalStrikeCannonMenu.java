package com.shand1an.sreln.screen;

import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.OrbitalStrikeCannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class OrbitalStrikeCannonMenu extends AbstractContainerMenu {

    public final OrbitalStrikeCannonBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Level level;

    public OrbitalStrikeCannonMenu(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        this(containerId, playerInv, getBlockEntity(playerInv, extraData.readBlockPos()));
    }

    public OrbitalStrikeCannonMenu(int containerId, Inventory playerInv, OrbitalStrikeCannonBlockEntity blockEntity) {
        super(ModMenuTypes.ORBITAL_STRIKE_CANNON.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.level = playerInv.player.level();
    }

    private static OrbitalStrikeCannonBlockEntity getBlockEntity(Inventory playerInv, BlockPos pos) {
        Level level = playerInv.player.level();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof OrbitalStrikeCannonBlockEntity cannon) {
            return cannon;
        }
        throw new IllegalStateException("Block entity is not OrbitalStrikeCannonBlockEntity at " + pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ORBITAL_STRIKE_CANNON.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0) {
            BlockPos nearest = OrbitalStrikeCannonBlockEntity.findNearestTargetDesignator(level, blockEntity.getBlockPos());
            if (nearest != null) {
                blockEntity.setTargetPos(nearest);
            }
            return true;
        }
        if (buttonId == 1) {
            blockEntity.startFiring();
            return true;
        }
        return false;
    }
}
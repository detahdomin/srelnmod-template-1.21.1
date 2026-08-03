package com.shand1an.sreln.block.info;

import com.shand1an.sreln.block.ModBlockEntities;
import com.shand1an.sreln.screen.InfoTerminalMenu;
import com.shand1an.sreln.screen.TerminalFileSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InfoTerminalBlockEntity extends BlockEntity implements MenuProvider {

    public InfoTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFO_TERMINAL.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.sreln_mod.info_terminal");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new InfoTerminalMenu(containerId, playerInv, this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        String ip = String.format("10.0.%d.%d", Math.abs(worldPosition.getX()) % 256, Math.abs(worldPosition.getZ()) % 256);
        TerminalFileSystem.clearUserData(ip);
    }
}
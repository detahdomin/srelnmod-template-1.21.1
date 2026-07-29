package com.shand1an.sreln.item;

import com.shand1an.sreln.block.LightingConsoleBlockEntity;
import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.terminal.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TerminalBinderItem extends Item {
    public TerminalBinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null) return InteractionResult.FAIL;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Block clickedBlock = level.getBlockState(pos).getBlock();
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof LightingConsoleBlockEntity console) {
            transferToConsole(stack, console, player);
            return InteractionResult.SUCCESS;
        }

        if (clickedBlock == ModBlocks.TERMINAL.get()) {
            bindTerminal(stack, pos, player);
            return InteractionResult.SUCCESS;
        }

        if (clickedBlock instanceof RedstoneLampBlock) {
            addLamp(stack, pos, player);
            return InteractionResult.SUCCESS;
        }

        player.sendSystemMessage(Component.literal("请右键终端、红石灯或灯光控制器"));
        return InteractionResult.SUCCESS;
    }

    private CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void bindTerminal(ItemStack stack, BlockPos pos, Player player) {
        CompoundTag tag = getOrCreateTag(stack);
        ListTag terminals = tag.getList("Terminals", Tag.TAG_COMPOUND);
        for (int i = 0; i < terminals.size(); i++) {
            CompoundTag t = terminals.getCompound(i);
            if (t.getInt("X") == pos.getX() && t.getInt("Y") == pos.getY() && t.getInt("Z") == pos.getZ()) {
                player.sendSystemMessage(Component.literal("该终端已绑定"));
                return;
            }
        }
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX()); posTag.putInt("Y", pos.getY()); posTag.putInt("Z", pos.getZ());
        terminals.add(posTag);
        tag.put("Terminals", terminals);
        saveTag(stack, tag);
        player.sendSystemMessage(Component.literal("已绑定终端 " + terminals.size() + ": " + pos.toShortString()));
    }

    private void addLamp(ItemStack stack, BlockPos pos, Player player) {
        CompoundTag tag = getOrCreateTag(stack);
        ListTag lamps = tag.getList("Lamps", Tag.TAG_COMPOUND);
        for (int i = 0; i < lamps.size(); i++) {
            CompoundTag t = lamps.getCompound(i);
            if (t.getInt("X") == pos.getX() && t.getInt("Y") == pos.getY() && t.getInt("Z") == pos.getZ()) {
                lamps.remove(i);
                tag.put("Lamps", lamps);
                saveTag(stack, tag);
                player.sendSystemMessage(Component.literal("已移除红石灯: " + pos.toShortString() + "（共" + lamps.size() + "个）"));
                return;
            }
        }
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX()); posTag.putInt("Y", pos.getY()); posTag.putInt("Z", pos.getZ());
        lamps.add(posTag);
        tag.put("Lamps", lamps);
        saveTag(stack, tag);
        player.sendSystemMessage(Component.literal("已添加红石灯: " + pos.toShortString() + "（共" + lamps.size() + "个）"));
    }

    private void transferToConsole(ItemStack stack, LightingConsoleBlockEntity console, Player player) {
        CompoundTag tag = getOrCreateTag(stack);
        ListTag terminals = tag.getList("Terminals", Tag.TAG_COMPOUND);
        ListTag lamps = tag.getList("Lamps", Tag.TAG_COMPOUND);
        if (terminals.isEmpty() && lamps.isEmpty()) {
            player.sendSystemMessage(Component.literal("绑定器为空，请先右键终端和红石灯"));
            return;
        }
        for (int i = 0; i < terminals.size(); i++) {
            CompoundTag posTag = terminals.getCompound(i);
            BlockPos termPos = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
            console.addTerminal(termPos);
            BlockEntity termBe = console.getLevel().getBlockEntity(termPos);
            if (termBe instanceof TerminalBlockEntity terminal) {
                terminal.setConsolePos(console.getBlockPos());
            }
        }
        for (int i = 0; i < lamps.size(); i++) {
            CompoundTag posTag = lamps.getCompound(i);
            console.addLamp(new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z")));
        }
        saveTag(stack, new CompoundTag());
        stack.shrink(1);
        player.sendSystemMessage(Component.literal("已传输到灯光控制器！终端" + terminals.size() + "个，红石灯" + lamps.size() + "个"));
    }
}
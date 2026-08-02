package com.shand1an.sreln.item;

import com.shand1an.sreln.block.facility.FacilityTerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FacilityBinderItem extends Item {
    public FacilityBinderItem(Properties properties) {
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

        if (be instanceof FacilityTerminalBlockEntity terminal) {
            transferToTerminal(stack, terminal, player);
            return InteractionResult.SUCCESS;
        }

        if (clickedBlock instanceof LeverBlock || clickedBlock == Blocks.LEVER) {
            bindLever(stack, pos, player);
            return InteractionResult.SUCCESS;
        }

        player.sendSystemMessage(Component.literal("请右键拉杆或设施终端"));
        return InteractionResult.SUCCESS;
    }

    private CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    private void saveTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void bindLever(ItemStack stack, BlockPos pos, Player player) {
        CompoundTag tag = getOrCreateTag(stack);
        if (tag.contains("LeverX")) {
            player.sendSystemMessage(Component.literal("该绑定器已绑定拉杆: (" + tag.getInt("LeverX") + ", " + tag.getInt("LeverY") + ", " + tag.getInt("LeverZ") + ")"));
            return;
        }
        tag.putInt("LeverX", pos.getX());
        tag.putInt("LeverY", pos.getY());
        tag.putInt("LeverZ", pos.getZ());
        saveTag(stack, tag);
        player.sendSystemMessage(Component.literal("已绑定拉杆: " + pos.toShortString()));
    }

    private void transferToTerminal(ItemStack stack, FacilityTerminalBlockEntity terminal, Player player) {
        CompoundTag tag = getOrCreateTag(stack);
        if (!tag.contains("LeverX")) {
            player.sendSystemMessage(Component.literal("绑定器未绑定拉杆，请先右键拉杆"));
            return;
        }
        BlockPos leverPos = new BlockPos(tag.getInt("LeverX"), tag.getInt("LeverY"), tag.getInt("LeverZ"));
        String name = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName().getString() : "设施#" + (terminal.getFacilityCount() + 1);
        terminal.addFacility(name, leverPos);
        saveTag(stack, new CompoundTag());
        stack.shrink(1);
        player.sendSystemMessage(Component.literal("已添加设施 '" + name + "' 到终端"));
    }
}
package com.shand1an.sreln.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HackerBadgeItem extends Item {
    public HackerBadgeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.getTags().contains("hacker")) {
                player.sendSystemMessage(Component.literal("You already have the hacker tag."));
            } else {
                player.addTag("hacker");
                player.sendSystemMessage(Component.literal("Hacker tag granted."));
            }
        }
        return InteractionResultHolder.success(stack);
    }
}
package com.shand1an.sreln.item;

import com.shand1an.sreln.entity.AssaultEnderPearlEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AssaultEnderPearlItem extends Item {

    private static final String[] ROMAN = {"", "I", "II", "III", "IV", "V"};

    public AssaultEnderPearlItem(Properties properties) {
        super(properties);
    }

    private static String toRoman(int level) {
        return level >= 1 && level < ROMAN.length ? ROMAN[level] : String.valueOf(level);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        int level = PearlLevelUtil.getPearlLevel(stack);
        if (level > 1) {
            tooltip.add(Component.literal("等级 " + toRoman(level))
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            AssaultEnderPearlEntity pearl = new AssaultEnderPearlEntity(level, player);
            pearl.setPearlLevel(PearlLevelUtil.getPearlLevel(stack));
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);
            level.addFreshEntity(pearl);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
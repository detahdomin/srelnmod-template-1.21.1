package com.shand1an.sreln.item;

import com.shand1an.sreln.entity.SplinterEnderPearlEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SplinterEnderPearlItem extends Item {
    public SplinterEnderPearlItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int realIndex = level.random.nextInt(3);
            for (int i = 0; i < 3; i++) {
                SplinterEnderPearlEntity pearl = new SplinterEnderPearlEntity(level, player);
                pearl.setReal(i == realIndex);
                float offset = (i - 1) * 10.0F;
                pearl.shootFromRotation(player, player.getXRot(), player.getYRot() + offset, 0.0F, 2.0F, 1.0F);
                level.addFreshEntity(pearl);
            }
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
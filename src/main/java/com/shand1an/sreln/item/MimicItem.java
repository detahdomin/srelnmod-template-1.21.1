package com.shand1an.sreln.item;

import com.shand1an.sreln.MimicHandler;
import com.shand1an.sreln.srelnMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class MimicItem extends Item {

    public MimicItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        CompoundTag tag = new CompoundTag();
        tag.putString("MimicEntity", key.toString());
        ItemStack handStack = player.getItemInHand(hand);
        handStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        player.setItemInHand(hand, handStack);
        player.displayClientMessage(
                Component.translatable("item.sreln_mod.mimic.captured", target.getName().getString()),
                true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        srelnMod.LOGGER.info("[MimicItem] use(): isClient={}, isShift={}, hand={}, stackHash={}", level.isClientSide, player.isShiftKeyDown(), hand, System.identityHashCode(stack));

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                MimicHandler.clearMimic(player);
                player.displayClientMessage(Component.translatable("item.sreln_mod.mimic.cleared"), true);
            }
            return InteractionResultHolder.success(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        srelnMod.LOGGER.info("[MimicItem] Server-side stackHash={}, customData={}", System.identityHashCode(stack), customData != null ? "present" : "NULL");
        if (customData == null) {
            return InteractionResultHolder.fail(stack);
        }
        CompoundTag tag = customData.copyTag();
        srelnMod.LOGGER.info("[MimicItem] NBT contains MimicEntity: {}", tag.contains("MimicEntity"));
        if (!tag.contains("MimicEntity")) {
            return InteractionResultHolder.fail(stack);
        }

        String typeStr = tag.getString("MimicEntity");
        srelnMod.LOGGER.info("[MimicItem] Attempting to activate mimic: {}", typeStr);
        ResourceLocation key = ResourceLocation.tryParse(typeStr);
        if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
            MimicHandler.setMimic(player, type);
            player.displayClientMessage(
                    Component.translatable("item.sreln_mod.mimic.activated"),
                    true);
        }
        return InteractionResultHolder.success(stack);
    }
}
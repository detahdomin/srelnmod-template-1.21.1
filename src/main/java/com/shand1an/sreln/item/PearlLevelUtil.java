package com.shand1an.sreln.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class PearlLevelUtil {

    public static int getPearlLevel(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null
                ? Math.max(1, customData.copyTag().getInt("PearlLevel"))
                : 1;
    }

    public static void setPearlLevel(ItemStack stack, int level) {
        CompoundTag tag = new CompoundTag();
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        if (existing != null) {
            tag = existing.copyTag();
        }
        tag.putInt("PearlLevel", level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
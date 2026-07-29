package com.shand1an.sreln.item;

import com.shand1an.sreln.entity.ModEntityTypes;
import com.shand1an.sreln.entity.TargetDesignatorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TargetDesignatorItem extends Item {

    public TargetDesignatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        Vec3 spawnPos = pos.getCenter().add(face.getStepX() * 0.55, face.getStepY() * 0.55, face.getStepZ() * 0.55);

        TargetDesignatorEntity entity = ModEntityTypes.TARGET_DESIGNATOR.get().create(level);
        if (entity != null) {
            entity.setPos(spawnPos);
            entity.setYRot(context.getPlayer() != null ? context.getPlayer().getYRot() : 0);
            level.addFreshEntity(entity);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.setChunkForced(entity.chunkPosition().x, entity.chunkPosition().z, true);
            }
            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.CONSUME;
    }
}
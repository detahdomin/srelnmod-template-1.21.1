package com.shand1an.sreln;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MimicHandler {

    private static final Map<UUID, EntityType<?>> CLIENT_MIMICS = new HashMap<>();
    private static final Map<UUID, EntityType<?>> SERVER_MIMICS = new HashMap<>();

    public static void setMimic(Player player, EntityType<?> type) {
        srelnMod.LOGGER.info("[MimicHandler] setMimic called, player={}, type={}", player.getName().getString(), type);
        SERVER_MIMICS.put(player.getUUID(), type);
        player.getPersistentData().putString("MimicEntity",
                BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        PacketDistributor.sendToAllPlayers(new MimicSyncPayload(player.getUUID(), type));
    }

    public static void clearMimic(Player player) {
        srelnMod.LOGGER.info("[MimicHandler] clearMimic called, player={}", player.getName().getString());
        SERVER_MIMICS.remove(player.getUUID());
        player.getPersistentData().remove("MimicEntity");
        PacketDistributor.sendToAllPlayers(new MimicSyncPayload(player.getUUID(), null));
    }

    public static void syncOnLogin(Player player) {
        String typeStr = player.getPersistentData().getString("MimicEntity");
        srelnMod.LOGGER.info("[MimicHandler] syncOnLogin, player={}, persistentData={}", player.getName().getString(), typeStr.isEmpty() ? "empty" : typeStr);
        if (typeStr.isEmpty()) return;
        ResourceLocation key = ResourceLocation.tryParse(typeStr);
        if (key != null && BuiltInRegistries.ENTITY_TYPE.containsKey(key)) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
            SERVER_MIMICS.put(player.getUUID(), type);
            PacketDistributor.sendToAllPlayers(new MimicSyncPayload(player.getUUID(), type));
        }
    }

    public static EntityType<?> getMimicType(UUID playerUUID) {
        return CLIENT_MIMICS.get(playerUUID);
    }

    public static void handleClientSync(MimicSyncPayload payload, IPayloadContext context) {
        srelnMod.LOGGER.info("[MimicHandler] handleClientSync received, player={}, type={}", payload.playerUUID, payload.entityType);
        context.enqueueWork(() -> {
            if (payload.entityType() == null) {
                CLIENT_MIMICS.remove(payload.playerUUID());
            } else {
                CLIENT_MIMICS.put(payload.playerUUID(), payload.entityType());
            }
            srelnMod.LOGGER.info("[MimicHandler] CLIENT_MIMICS updated, size={}", CLIENT_MIMICS.size());
        });
    }

    public record MimicSyncPayload(UUID playerUUID, EntityType<?> entityType) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<MimicSyncPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(srelnMod.MODID, "mimic_sync"));

        public static final StreamCodec<FriendlyByteBuf, MimicSyncPayload> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public MimicSyncPayload decode(FriendlyByteBuf buf) {
                UUID uuid = buf.readUUID();
                boolean hasEntity = buf.readBoolean();
                if (hasEntity) {
                    ResourceLocation key = buf.readResourceLocation();
                    EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
                    return new MimicSyncPayload(uuid, type);
                }
                return new MimicSyncPayload(uuid, null);
            }

            @Override
            public void encode(FriendlyByteBuf buf, MimicSyncPayload payload) {
                buf.writeUUID(payload.playerUUID);
                buf.writeBoolean(payload.entityType != null);
                if (payload.entityType != null) {
                    buf.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(payload.entityType));
                }
            }
        };

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
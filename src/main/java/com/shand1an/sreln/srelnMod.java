package com.shand1an.sreln;
import com.shand1an.sreln.block.ModBlockEntities;
import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.block.OrbitalStrikeCannonBlockEntity;
import com.shand1an.sreln.entity.ModEntityTypes;
import com.shand1an.sreln.entity.TargetDesignatorEntity;
import com.shand1an.sreln.effect.ModMobEffects;
import com.shand1an.sreln.item.ModCreativeModeTabs;
import com.shand1an.sreln.item.ModItems;
import com.shand1an.sreln.item.TravelerKeyItem;
import com.shand1an.sreln.recipe.ModRecipes;
import com.shand1an.sreln.screen.ModMenuTypes;
import com.shand1an.sreln.sound.ModSounds;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import com.shand1an.sreln.entity.FakePlayerEntity;
import com.shand1an.sreln.entity.TargetDesignatorEntity;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.shand1an.sreln.block.LightingConsoleBlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(srelnMod.MODID)
public class srelnMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "sreln_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public srelnMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::entityAttributeCreation);
        ModEntityTypes.register(modEventBus);
        ModMobEffects.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModSounds.register(modEventBus);
        registerPayloadHandlers(modEventBus);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (srelnMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    private void entityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FAKE_PLAYER.get(), FakePlayerEntity.createAttributes().build());
        event.put(ModEntityTypes.SUMMONED_WARDEN.get(), net.minecraft.world.entity.monster.warden.Warden.createAttributes().build());
        event.put(ModEntityTypes.TARGET_DESIGNATOR.get(), TargetDesignatorEntity.createAttributes().build());
    }
    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    private void registerPayloadHandlers(IEventBus modEventBus) {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, (event) -> {
            PayloadRegistrar registrar = event.registrar(MODID);
            registrar.playToClient(
                    MimicHandler.MimicSyncPayload.TYPE,
                    MimicHandler.MimicSyncPayload.STREAM_CODEC,
                    MimicHandler::handleClientSync);
        });
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MimicHandler.syncOnLogin(event.getEntity());
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof TargetDesignatorEntity && event.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.setChunkForced(event.getEntity().chunkPosition().x, event.getEntity().chunkPosition().z, true);
            OrbitalStrikeCannonBlockEntity.registerDesignator(serverLevel.dimension(), event.getEntity().blockPosition());
        }
    }

    private static final int SEARCH_RADIUS = 128;
    private static final int MAX_DOORS = 200;
    private static final int CHARGE_TICKS = 60;
    private static final Map<UUID, PendingTeleport> pendingTeleports = new HashMap<>();

    private record PendingTeleport(Player player, Vec3 destination, int remainingTicks) {}

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        Iterator<Map.Entry<UUID, PendingTeleport>> it = pendingTeleports.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PendingTeleport> entry = it.next();
            PendingTeleport pt = entry.getValue();
            if (pt.player.isRemoved() || !pt.player.isAlive()) {
                it.remove();
                continue;
            }
            int newTicks = pt.remainingTicks - 1;
            if (newTicks <= 0) {
                pt.player.teleportTo(pt.destination.x, pt.destination.y, pt.destination.z);
                pt.player.resetFallDistance();
                spawnTeleportParticles((ServerLevel) pt.player.level(), pt.destination);
                it.remove();
            } else {
                entry.setValue(new PendingTeleport(pt.player, pt.destination, newTicks));
            }
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!(stack.getItem() instanceof TravelerKeyItem)) return;

        if (pendingTeleports.containsKey(player.getUUID())) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        if (!(state.getBlock() instanceof DoorBlock)) return;

        ServerLevel serverLevel = (ServerLevel) event.getLevel();
        BlockPos lowerPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;

        BlockPos targetDoor = findRandomDoor(serverLevel, lowerPos, SEARCH_RADIUS);
        if (targetDoor == null) {
            player.sendSystemMessage(Component.translatable("item.sreln_mod.traveler_key.no_door"));
            return;
        }

        BlockPos landingPos = findSafeLandingPos(serverLevel, targetDoor);
        if (landingPos == null) {
            player.sendSystemMessage(Component.translatable("item.sreln_mod.traveler_key.blocked"));
            return;
        }

        Vec3 from = player.position();
        Vec3 to = landingPos.getBottomCenter();

        serverLevel.playSound(null, from.x, from.y, from.z,
                ModSounds.TRANSMISSION_SOUND.get(), SoundSource.BLOCKS, 2.0F, 1.0F);

        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CHARGE_TICKS, 5, false, false, true));

        spawnTeleportParticles(serverLevel, from);

        if (!player.isCreative()) {
            stack.shrink(1);
        }

        pendingTeleports.put(player.getUUID(), new PendingTeleport(player, to, CHARGE_TICKS));
    }

    private static void spawnTeleportParticles(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + 1.0, pos.z,
                80, 0.5, 1.0, 0.5, 0.05);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                pos.x, pos.y + 1.0, pos.z,
                40, 0.3, 0.8, 0.3, 0.03);
    }

    private static BlockPos findRandomDoor(ServerLevel level, BlockPos exclude, int radius) {
        List<BlockPos> doors = new ArrayList<>();
        int r = radius;

        for (int dx = -r; dx <= r; dx += 16) {
            for (int dz = -r; dz <= r; dz += 16) {
                int chunkX = (exclude.getX() + dx) >> 4;
                int chunkZ = (exclude.getZ() + dz) >> 4;
                if (!level.hasChunk(chunkX, chunkZ)) continue;

                int minX = Math.max(chunkX << 4, exclude.getX() - r);
                int maxX = Math.min((chunkX + 1) << 4, exclude.getX() + r);
                int minZ = Math.max(chunkZ << 4, exclude.getZ() - r);
                int maxZ = Math.min((chunkZ + 1) << 4, exclude.getZ() + r);

                for (int x = minX; x < maxX; x++) {
                    for (int z = minZ; z < maxZ; z++) {
                        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() && doors.size() < MAX_DOORS; y++) {
                            BlockPos check = new BlockPos(x, y, z);
                            if (check.equals(exclude)) continue;
                            BlockState bs = level.getBlockState(check);
                            if (bs.getBlock() instanceof DoorBlock && bs.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                                doors.add(check.immutable());
                            }
                        }
                    }
                }
            }
        }

        if (doors.isEmpty()) {
            LOGGER.info("[TravelerKey] findRandomDoor: 0 doors found in radius {}", radius);
            return null;
        }

        LOGGER.info("[TravelerKey] findRandomDoor: {} doors found in radius {}", doors.size(), radius);
        Collections.shuffle(doors);
        return doors.get(0);
    }

    private static BlockPos findSafeLandingPos(ServerLevel level, BlockPos doorPos) {
        BlockState doorState = level.getBlockState(doorPos);
        Direction facing = doorState.getValue(DoorBlock.FACING);

        BlockPos behind = doorPos.relative(facing.getOpposite());
        BlockPos candidate = behind;

        BlockState bs = level.getBlockState(candidate);
        if (!bs.isSolid() && bs.getBlock() != Blocks.AIR && !bs.isAir()) {
            candidate = behind.above();
        }

        BlockState ground = level.getBlockState(candidate);
        if (ground.isSolid() && !ground.getBlock().equals(Blocks.AIR)) {
            candidate = candidate.above();
        }

        BlockState feet = level.getBlockState(candidate);
        BlockState head = level.getBlockState(candidate.above());
        BlockState groundBelow = level.getBlockState(candidate.below());

        if ((feet.isAir() || !feet.isSolid()) && (head.isAir() || !head.isSolid()) && groundBelow.isSolid()) {
            return candidate;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos alt = candidate.offset(dx, 0, dz);
                BlockState altFeet = level.getBlockState(alt);
                BlockState altHead = level.getBlockState(alt.above());
                BlockState altGround = level.getBlockState(alt.below());
                if ((altFeet.isAir() || !altFeet.isSolid()) && (altHead.isAir() || !altHead.isSolid()) && altGround.isSolid()) {
                    return alt;
                }
            }
        }

        return null;
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getState().getBlock() instanceof RedstoneLampBlock)) return;
        BlockPos pos = event.getPos();
        Level level = (Level) event.getLevel();
        for (BlockEntity be : level.getChunkAt(pos).getBlockEntities().values()) {
            if (be instanceof LightingConsoleBlockEntity console) {
                console.removeLamp(pos);
            }
        }
    }
}
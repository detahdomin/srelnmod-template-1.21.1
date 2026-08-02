package com.shand1an.sreln.block;

import com.shand1an.sreln.block.corpse.CorpseBlockEntity;
import com.shand1an.sreln.block.facility.FacilityTerminalBlockEntity;
import com.shand1an.sreln.block.terminal.TerminalBlockEntity;
import com.shand1an.sreln.srelnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, srelnMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OrbitalStrikeCannonBlockEntity>> ORBITAL_STRIKE_CANNON =
            BLOCK_ENTITIES.register("orbital_strike_cannon",
                    () -> BlockEntityType.Builder.of(
                            OrbitalStrikeCannonBlockEntity::new,
                            ModBlocks.ORBITAL_STRIKE_CANNON.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightingConsoleBlockEntity>> LIGHTING_CONSOLE =
            BLOCK_ENTITIES.register("lighting_console",
                    () -> BlockEntityType.Builder.of(
                            LightingConsoleBlockEntity::new,
                            ModBlocks.LIGHTING_CONSOLE.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TerminalBlockEntity>> TERMINAL =
            BLOCK_ENTITIES.register("terminal",
                    () -> BlockEntityType.Builder.of(
                            TerminalBlockEntity::new,
                            ModBlocks.TERMINAL.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FacilityTerminalBlockEntity>> FACILITY_TERMINAL =
            BLOCK_ENTITIES.register("facility_terminal",
                    () -> BlockEntityType.Builder.of(
                            FacilityTerminalBlockEntity::new,
                            ModBlocks.FACILITY_TERMINAL.get()
                    ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CorpseBlockEntity>> CORPSE =
            BLOCK_ENTITIES.register("corpse",
                    () -> BlockEntityType.Builder.of(
                            CorpseBlockEntity::new,
                            ModBlocks.CORPSE_HANGING_MALE.get(),
                            ModBlocks.CORPSE_BEHEADED_MALE.get(),
                            ModBlocks.CORPSE_DISMEMBERED_FEMALE.get(),
                            ModBlocks.CORPSE_DISMEMBERED_MALE.get(),
                            ModBlocks.CORPSE_BROKEN_FEMALE.get(),
                            ModBlocks.CORPSE_CRUSHED_HEAD_MALE.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
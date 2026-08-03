package com.shand1an.sreln.item;

import com.shand1an.sreln.block.ModBlocks;
import com.shand1an.sreln.fluid.ModFluids;
import com.shand1an.sreln.srelnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, srelnMod.MODID);

    public static final Supplier<CreativeModeTab> INSTITUTE_TAB =
            CREATIVE_MODE_TABS.register("institute_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.TERMINAL.asItem()))
                    .title(net.minecraft.network.chat.Component.literal("研究所"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.Recall_Ender_Pearl);
                        output.accept(ModItems.Harming_Ender_Pearl);
                        output.accept(ModItems.Lingering_Ender_Pearl);
                        output.accept(ModItems.Swap_Ender_Pearl);
                        output.accept(ModItems.Splinter_Ender_Pearl);
                        output.accept(ModItems.Skyward_Ender_Pearl);
                        output.accept(ModItems.Ricochet_Ender_Pearl);
                        output.accept(ModItems.Echo_Ender_Pearl);
                        output.accept(ModItems.Swift_Ender_Pearl);
                        output.accept(ModItems.Assault_Ender_Pearl);
                        output.accept(ModItems.TARGET_DESIGNATOR);
                        output.accept(ModItems.MIMIC);
                        output.accept(ModItems.TRAVELER_KEY);
                        output.accept(ModItems.TERMINAL_BINDER);
                        output.accept(ModItems.FACILITY_BINDER);
                        output.accept(ModItems.HACKER_BADGE);
                        output.accept(ModFluids.CHEMICAL_TOXIC_BUCKET);
                        output.accept(ModFluids.CRUDE_OIL_BUCKET);
                        output.accept(ModBlocks.ORBITAL_STRIKE_CANNON);
                        output.accept(ModBlocks.WHITE_POOL_TILE);
                        output.accept(ModBlocks.SALVATION_RESEARCH_INSTITUTE_LIGHT);
                        output.accept(ModBlocks.WHITE_CERAMIC_TILE_PILLAR);
                        output.accept(ModBlocks.LIGHTING_CONSOLE);
                        output.accept(ModBlocks.TERMINAL);
                        output.accept(ModBlocks.FACILITY_TERMINAL);
                        output.accept(ModBlocks.INFO_TERMINAL);
                        output.accept(ModBlocks.CORPSE_HANGING_MALE);
                        output.accept(ModBlocks.CORPSE_BEHEADED_MALE);
                        output.accept(ModBlocks.CORPSE_DISMEMBERED_FEMALE);
                        output.accept(ModBlocks.CORPSE_DISMEMBERED_MALE);
                        output.accept(ModBlocks.CORPSE_BROKEN_FEMALE);
                        output.accept(ModBlocks.CORPSE_CRUSHED_HEAD_MALE);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
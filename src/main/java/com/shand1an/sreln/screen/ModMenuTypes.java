package com.shand1an.sreln.screen;

import com.shand1an.sreln.srelnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, srelnMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<OrbitalStrikeCannonMenu>> ORBITAL_STRIKE_CANNON =
            MENU_TYPES.register("orbital_strike_cannon",
                    () -> IMenuTypeExtension.create(OrbitalStrikeCannonMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TerminalMenu>> TERMINAL =
            MENU_TYPES.register("terminal",
                    () -> IMenuTypeExtension.create(TerminalMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<LightingConsoleMenu>> LIGHTING_CONSOLE =
            MENU_TYPES.register("lighting_console",
                    () -> IMenuTypeExtension.create(LightingConsoleMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FacilityTerminalMenu>> FACILITY_TERMINAL =
            MENU_TYPES.register("facility_terminal",
                    () -> IMenuTypeExtension.create(FacilityTerminalMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}